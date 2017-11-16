package com.ngs.bigquery.github.actors

import akka.actor.{Actor, Props}

import scala.collection.mutable.{ListBuffer}
import scala.concurrent.duration._
import akka.actor.Cancellable
import org.joda.time.DateTime
import bigquery4s._


object GithubCollectorActor {

  def props = Props(classOf[GithubCollectorActor], Int)

  sealed trait CollectorMsg
  case object StartSystemMsg extends CollectorMsg       //start the query process
  case object CollectCommitsMsg extends CollectorMsg    //perform a BigQuery query
  case object TerminateSystemMsg extends CollectorMsg   //terminate the application

  //used to keep iterations of running average
  case class RunningAverage( count: Long, total: Long )


  /*
    input:
    Map[Int, Long] where
    the Int key represents the length of the github commit message
    and the Long value represents the frequency of the key in the data set.

    returns:
    Map[Int, Long] : the input map used to determine the mean and median.
    Long : number of values in the mean calulation.
    Long : sum of values in the mean calulation.
    Double : the median of the data set.
   */

  def getMedian( m: Map[Int, Long] ): ( Map[Int, Long], Long, Long, Double ) = {

    val keys = m.keySet.toSeq.sorted
    val tlen = keys.foldLeft( 0L ){ (ac, k) => { ac + m(k) } }
    val tsum = keys.foldLeft( 0L ){ (ac, k) => { ac + k*m(k) } }
    val medianpos = (if ( tlen % 2 == 0 ) List(tlen/2 -1, tlen/2) else List(tlen/2) )

    var pmedianpos = medianpos
    var pmedians = List.empty[Int]
    var pkeys = keys
    var offset = 0L
    var done = false
    while ( !done && !pkeys.isEmpty) {
      val ahead = m(pkeys.head)
      if ( pmedianpos.head < offset + ahead) {
        pmedians = pkeys.head :: pmedians
        if ( 1 < pmedianpos.length)
          pmedianpos = pmedianpos.tail
        else done = true
      }
      else {
        offset += ahead
        pkeys = pkeys.tail
      }
    }

    ( m, tlen, tsum, pmedians.sum.toDouble/pmedians.length.toDouble )
  }

  /*
    collectCommitLengths

    Queries the Google BigQuery Github repo for
    commit messages. Queries happen at 1 hour intervals,
    n times where n is determined from the command line and is by
    default equal to 3.

    returns:
    A Map[Int, Long] : A Map of message length, message length frequency pair.

   */

  def collectCommitLengths(): Map[Int, Long] = {
    val bq = BigQuery()
    val ds = bq.listDatasets("bigquery-public-data")
    val yourOwnProjectId = "ngsmith-25293"

    val timeWindow = System.currentTimeMillis/1000 -3600

    val q = s"SELECT LENGTH( message ) FROM [bigquery-public-data:github_repos.commits] AS gh WHERE ( ${timeWindow} < committer.time_sec )"

    val jobId = bq.startQuery(yourOwnProjectId, q)
    val job = bq.await(jobId)
    val rows = bq.getRows(job)

    rows.foldLeft( Map.empty[Int, Long] ){ (ac, wtr) => {
      wtr.cells.foldLeft( ac ){ ( acc, wtc ) => wtc.value match {
        case Some( k ) =>  acc + ( k.toString.toInt -> (acc.getOrElse(k.toString.toInt, 0L) +1L) )
        case _ => throw new RuntimeException("Bad value.")
      } }
    } }
  }

  def getTime(): String = DateTime.now.toString

}

class GithubCollectorActor( n: Int ) extends Actor {
  import GithubCollectorActor._
  import context._

  val avgs = ListBuffer.empty[RunningAverage] // list of cumulative averages
  var cumCommits = Map.empty[Int, Long]
  val medians = ListBuffer.empty[Double]      // list of cumulative medians
  var cycles = 0                              // number of queries to perform (once per hour)
  var scheduler = Option.empty[Cancellable]   // scheduler for periodic query invokations

  def receive = {
    case StartSystemMsg =>
        //start the scheduler
      scheduler = Some(context.system.scheduler.schedule( 0 seconds, 1 hour, self, CollectCommitsMsg ))

    case CollectCommitsMsg =>
      //perform the statistics

      val commits = collectCommitLengths()
      //get cumulative stats
      val ( m, alen, asum, med ) = getMedian( cumCommits ++ commits )

      cumCommits = m
      avgs += RunningAverage( alen, asum )
      medians += med

      println( s"time: ${getTime}")
      println( s"runnning average ${avgs.last.total.toDouble/avgs.last.count.toDouble}" )
      println( s"running median ${medians.last}" )
      println

      cycles += 1
      if ( n <= cycles) {
        scheduler.get.cancel()
        self ! TerminateSystemMsg
      }

    case TerminateSystemMsg =>
      //terminate the application
      context.system.terminate()
  }

}


