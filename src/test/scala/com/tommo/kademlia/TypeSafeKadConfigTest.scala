package com.tommo.kademlia

import com.tommo.kademlia.protocol.Host
import com.tommo.kademlia.identity.Id
import com.typesafe.config._

import scala.concurrent.duration._
import java.util.Properties

class KadConfigTest extends BaseUnitTest {
  test("load config file from tommo-kad namespace") {
	  val conf = TypeSafeKadConfig(); // loads config from /src/main/resources/application.conf TODO use testconfig -- needs to be bin
	  assert(conf.host == Host("127.0.0.1:2552"))

	  assert(conf.kBucketSize == 10)
	  assert(conf.refreshStaleKBucket == (600 second))
	  assert(conf.republishOriginal == (800 second))
	  
	  assert(conf.roundConcurrency == 2)
	  
	  assert(conf.roundTimeOut == (1 second))
	  assert(conf.requestTimeOut == (300 milliseconds))
	  
	  assert(conf.expireRemote == (700 seconds))
	  assert(conf.republishRemote == (600 seconds))
	  
	  assert(conf.name == "TestSystem")
	  
  }
  
  test("fail custom config does not contain the defined properties") {
    intercept[RuntimeException] {
    	new TypeSafeKadConfig(ConfigFactory.parseProperties(new Properties())) // empty properties
    }
  }
}

