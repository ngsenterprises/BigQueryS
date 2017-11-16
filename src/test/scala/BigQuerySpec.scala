package com.ngs.bigquery.github.test

import org.scalatest.{FlatSpec, Matchers}
import com.ngs.bigquery.github.actors.GithubCollectorActor._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import com.ngs.bigquery.github.BigQueryS.sys
import com.ngs.bigquery.github.actors.GithubCollectorActor
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


class GithubCommitMsgSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "GithubCollectorActor.median method" must {

    "determine the correct median" in {

      val m1 = Map[Int, Long]( (20 -> 2), (30 -> 3), (25 -> 2) )

      val res1 = GithubCollectorActor.getMedian( m1 )

      assert( res1._1 == m1 )
      //assert( res1._2 == 7 )
      //assert( res1._3 == 180 )
      assert( res1._4 == 25 )

      val m2 = m1 ++ Map[Int, Long]( (22 -> 2), (33 -> 3), (27 -> 2) )

      val res2 = GithubCollectorActor.getMedian( m2 )

      assert( res2._1 == m2 )
      //assert( res2._2 == 14 )
      //assert( res2._3 == 377 )
      assert( res2._4 == 27 )

    }
  }

  "GithubCollectorActor.median method" must {

    "determine the correct average" in {

      val m1 = Map[Int, Long]( (20 -> 2), (30 -> 3), (25 -> 2) )

      val res1 = GithubCollectorActor.getMedian( m1 )

      //assert( res1._1 == m1 )
      assert( res1._2 == 7 )
      assert( res1._3 == 180 )
      //assert( res1._4 == 25 )

      val m2 = m1 ++ Map[Int, Long]( (22 -> 2), (33 -> 3), (27 -> 2) )

      val res2 = GithubCollectorActor.getMedian( m2 )

      //assert( res2._1 == m2 )
      assert( res2._2 == 14 )
      assert( res2._3 == 377 )
      //assert( res2._4 == 27 )

    }
  }





}
