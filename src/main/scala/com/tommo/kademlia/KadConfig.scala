package com.tommo.kademlia

import com.typesafe.config._
import com.tommo.kademlia.protocol.Host
import TypeSafeKadConfig._
import scala.concurrent.duration._
import com.tommo.kademlia.identity.Id

import akka.util.Timeout

trait KadConfig {
	def host: Host
	def kBucketSize: Int
	def addressSpace: Int
	def concurrency: Int
	def roundTimeOut: FiniteDuration
	def id: Id
}

class TypeSafeKadConfig(config: Config) extends KadConfig {
  def this() = this(ConfigFactory.load())

  config.checkValid(ConfigFactory.defaultReference(), namespace)

  val host = Host(config.getString(s"${namespace}.host"), config.getInt(s"${namespace}.port"))

  val kBucketSize = config.getInt(s"${namespace}.kbucket-size")

  val addressSpace = config.getInt(s"${namespace}.address-space")
  
  val concurrency = config.getInt(s"${namespace}.concurrency")
  
  val roundTimeOut = FiniteDuration(config.getInt(s"${namespace}.round-timeout-ms"), MILLISECONDS)
  
  val id = Id(config.getString(s"${namespace}.id"))
  
}

object TypeSafeKadConfig {
  private def namespace = "tommo-kad"
  def apply() = new TypeSafeKadConfig()
}