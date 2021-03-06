package com.tommo.kademlia.routing

import scala.collection.immutable.TreeSet

import com.tommo.kademlia.BaseUnitTest
import com.tommo.kademlia.identity.Id

import com.tommo.kademlia.protocol.RemoteNode
import com.tommo.kademlia.protocol.Host
import com.tommo.kademlia.BaseFixture
class LastSeenOrderingTest extends BaseUnitTest with BaseFixture {

  implicit val ordering = LastSeenOrdering()

  test("ordered ascending by time") {
    val someNode = RemoteNode(mockHost, Id("anyString".getBytes()))

    val first = TimeStampNode(someNode, time = 1)
    val middle = TimeStampNode(someNode, time = 2)
    val last = TimeStampNode(someNode, time = 3)

    val sortedTree = TreeSet[TimeStampNode](middle, first, last)

    assert(sortedTree.max == last)
    assert(sortedTree.min == first)
  }
}