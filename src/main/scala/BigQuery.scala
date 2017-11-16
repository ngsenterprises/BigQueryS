package com.ngs.bigquery.github

import com.ngs.bigquery.github.actors._
import com.ngs.bigquery.github.actors.GithubCollectorActor._
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}


/*
  BigQueryS: for Google BigQuery in Scala.

  The application downloads Github commit message lengths
  and computes a running average and running median.

  The application takes a command line parameter as an Int,
  and is used to determine the number of iterations to query
  Google BigQuery. If no command line value the iterations defaults to 3.

  Output is displayed to std out as:

  time: 2017-11-15T13:43:57.087-08:00
  runnning average 31.733333333333334
  running median 22.0

 */


object BigQueryS extends App {

  //number of BigQuery queries
  val cycles =
    if ( 0 < args.length ) {
      Try( args.head.toInt ) match {
        case Failure( f ) => 3
        case Success( k ) => k
      }
    } else 3


  val sys = ActorSystem("BigQuerySystem")
  val app = sys.actorOf( Props( classOf[GithubCollectorActor], cycles ), name="GithubCollector")

  //start the deamon
  app ! StartSystemMsg

  Try( Await.ready( sys.whenTerminated, 24 hours ) ) match {
    case Success( t ) => { println("Success."); System.exit( 0 ) }
    case Failure( f ) => { println("Failure."); System.exit( 0 ) }
  }

}
