package com.tommo.kademlia.lookup

import akka.testkit.{ TestFSMRef, TestProbe }
import akka.actor.FSM._
import scala.collection.immutable.TreeMap
import scala.concurrent.duration._

import LookupValueFSM._
import LookupNodeFSM._
import com.tommo.kademlia.BaseFixture
import com.tommo.kademlia.BaseTestKit
import com.tommo.kademlia.protocol.Message.{ FindValueReply, FindValueRequest, CacheStoreRequest }
import com.tommo.kademlia.protocol.ActorNode
import com.tommo.kademlia.protocol.RequestDispatcher._
import com.tommo.kademlia.identity.Id
import com.tommo.kademlia.store.StoreActor._
import com.tommo.kademlia.protocol.Message._

class LookupValueFSMTest extends BaseTestKit("LookupFSMValueSpec") with BaseFixture {
  class Fixture extends LookupFixture(LookupValueFSMTest.this) {
    import mockConfig._

    val someRef = TestProbe().ref
    val storeProbe = TestProbe()
    
    val ref = TestFSMRef(new LookupValueFSM[Int](ActorNode(selfProbe.ref, id), kClosestProbe.ref, storeProbe.ref, kBucketSize, roundConcurrency, roundTimeOut))
    val listOfNodes = ActorNode(someRef, aRandomId(kBucketSize)) :: ActorNode(someRef, aRandomId(kBucketSize)) :: Nil
  }

  test("return FindValueRequest") {
    new Fixture {
      val searchId = aRandomId
      ref.underlyingActor.remoteKClosest(searchId, 4) shouldBe FindValueRequest(searchId, 4)
    }
  }
  
  test("query local store for value first") {
    new Fixture {
      val searchId = aRandomId
      ref ! FindValue(searchId)
      storeProbe.expectMsg(Get(searchId))
    }
  }
  
  test("if local store returns values go to finalize") {
    new Fixture {
      ref.setState(Initial, lookupReq)
      ref ! GetResult(Some(3, 10 seconds))
      awaitAssert(ref.stateName shouldBe FinalizeValue)
    }    
  }
  
  test("return result as Result(Left(value))") {
    new Fixture {
      ref.underlyingActor.returnResultsAs(aRandomId, listOfNodes) shouldBe LookupValueFSM.Result(Left(listOfNodes))
    }
  }

  test("if a value reply is received and it is a value goto FinalizeValue") {
    new Fixture {
      val qd = queryNodeDataDefault(2)

      ref.setState(QueryNode, qd)

      ref ! FindValueReply(Right(RemoteValue(3, 1 second)))

      awaitAssert(ref.stateName shouldBe FinalizeValue)
      
      expectMsg(LookupValueFSM.Result(Right(3)))
    }
  }
  
  test("if a value reply is received make store request to closest node that did not return the value") {
    new Fixture {
      val lookReq = Lookup(Id("1111"), testActor)
      val (s1, s2) = ((Id("1101"), NodeQuery(TestProbe().ref, 0 , true)), (Id("1100"), NodeQuery(TestProbe().ref, 0 , true)))
      val responded = (Id("0001"), NodeQuery(TestProbe().ref, 0 , false))
      
      val qd = QueryNodeData(lookReq, Map(), TreeMap(s1, s2)(new lookReq.id.SelfOrder), Map(responded))
      ref.setState(QueryNode, qd)
      
      val reply = FindValueReply(Right(RemoteValue(1, 1 second)))

      ref ! reply

      selfProbe.expectMsg(NodeRequest(s1._2.ref, CacheStoreRequest(qd.req.id, reply.result.right.get)))
    }
  }
}