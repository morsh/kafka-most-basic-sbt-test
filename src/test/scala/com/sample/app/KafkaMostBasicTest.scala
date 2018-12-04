package com.sample.app

import kafka.consumer._
import kafka.javaapi.consumer.ConsumerConnector
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.server.{KafkaConfig,KafkaServerStartable}
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.{KafkaProducer,Producer,ProducerRecord}
import org.junit.{After,Before,Test}

import java.io.IOException
import java.util.{HashMap,List,Map,Properties}
import collection.mutable.Stack

import org.hamcrest.core.Is.is
import org.junit.Assert.assertThat

import org.scalatest._

class KafkaMostBasicTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  private val topic: String = "topic1-" + System.currentTimeMillis

  private var server: KafkaTestFixture = _
  private var producer: Producer[String, String] = _
  private var consumerConnector: ConsumerConnector = _

  override def beforeEach() {
    this.server = new KafkaTestFixture()
    this.server.start(serverProperties())

    //Create a producer
    this.producer = new KafkaProducer[String, String](producerProps())

    //send a message
    this.producer.send(new ProducerRecord(topic, "message")).get()

  }

  override def afterEach() {
    this.producer.close()
    this.consumerConnector.shutdown()
    this.server.stop()
  }

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  "The kefka message" should "be 'message'" in {

    //Create a consumer
    val it: ConsumerIterator[String, String] = buildConsumer(topic)

    //read it back
    val messageAndMetadata: MessageAndMetadata[String, String] = it.next()
    val value: String = messageAndMetadata.message()
    value shouldEqual "message"
  }

  def buildConsumer(topic: String): ConsumerIterator[String, String] = {
      val props: Properties = consumerProperties()

      val topicCountMap: Map[String, Integer] = new HashMap()
      topicCountMap.put(topic, 1);
      
      val consumerConfig = new ConsumerConfig(props)
      consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig)
      
      val consumers: Map[String, List[KafkaStream[String, String]]] = consumerConnector.createMessageStreams(topicCountMap, new StringDecoder, new StringDecoder)
      val stream: KafkaStream[String, String] = consumers.get(topic).get(0)
      
      stream.iterator()
  }

  def consumerProperties(): Properties = {
      val props = new Properties()
      props.put("zookeeper.connect", serverProperties().get("zookeeper.connect"))
      props.put("group.id", "group1")
      props.put("auto.offset.reset", "smallest")
      props.put("zookeeper.session.timeout.ms", "30000")
      props.put("auto.commit.interval.ms", "1000")
      props.put("fetch.message.max.bytes", "4194304")
      props
  }

  private def producerProps(): Properties = {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("request.required.acks", "1")
      props.put("retries", "1")
      props.put("max.in.flight.requests.per.connection", "1")
      props
  }

  private def serverProperties(): Properties = {
    val props = new Properties()
    props.put("zookeeper.connect", "localhost:2181")
    props.put("broker.id", "1")
    props
  }

  private class KafkaTestFixture {
    private var zk: TestingServer = _
    private var kafka: KafkaServerStartable = _

    @throws[Exception]
    def start(properties: Properties) {
      val port: Integer = getZkPort(properties)
      this.zk = new TestingServer(port)
      this.zk.start()

      val kafkaConfig = new KafkaConfig(properties)
      this.kafka = new KafkaServerStartable(kafkaConfig)
      this.kafka.startup()
    }

    @throws[IOException]
    def stop()  {
      this.kafka.shutdown()
      this.zk.stop()
      this.zk.close()
    }

    private def getZkPort(properties: Properties): Integer = {
      val url: String = properties.get("zookeeper.connect").asInstanceOf[String]
      val port: String = url.split(":")(1)
      Integer.valueOf(port)
    }
  }
}
