package org.testcontainers.localStackmodule

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.SubscribeRequest
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


@Testcontainers
class QueueServiceTest {

    private val snsClient: AmazonSNS = AmazonSNSClientBuilder.standard()
        .withEndpointConfiguration(localStack.getEndpointConfiguration(SNS))
        .withCredentials(localStack.defaultCredentialsProvider)
        .build()

    private val sqsClient: AmazonSQS = AmazonSQSClientBuilder.standard()
        .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
        .withCredentials(localStack.defaultCredentialsProvider)
        .build()

    companion object {
        private const val LOCALSTACK = "localstack/localstack:0.13.0"
        @Container
        private val localStack = LocalStackContainer(DockerImageName.parse(LOCALSTACK))
            .withServices(SNS, SQS)
    }

    @Test
    fun `Should subscribe and publish a test message`() {
        val topicArn = snsClient.createTopic("topic-test").topicArn
        val queueResult = sqsClient.createQueue("queue-test")
        val messageTest = "test"
        val fooQueueUrl = queueResult.queueUrl

        subscribe(topicArn, fooQueueUrl)
        snsClient.publish(topicArn, messageTest)

        val messages = sqsClient.receiveMessage(fooQueueUrl).messages
        val message = messages.stream().findFirst()

        Assertions.assertEquals(1, messages.count())
        Assertions.assertEquals(messageTest, message.get().body)
    }

    private fun subscribe(topicArn: String?, fooQueueUrl: String?) {
        snsClient.subscribe(
            SubscribeRequest(topicArn, "sqs", fooQueueUrl)
                .withAttributes(mapOf("RawMessageDelivery" to "true"))
        )
    }
}