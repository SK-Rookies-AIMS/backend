package com.aims.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaCustomProperties {

    /**
     * Kafka / MSK Broker 목록
     */
    private List<String> bootstrapServers =
            List.of("localhost:9092");

    /**
     * 기본 Consumer Group
     */
    private String groupId =
            "backend-local";

    /**
     * Offset이 없을 경우 시작 위치
     */
    private String autoOffsetReset =
            "earliest";

    /**
     * Listener 활성화 여부
     */
    private boolean listenersEnabled =
            true;

    /**
     * PLAINTEXT / SASL_SSL
     */
    private String securityProtocol =
            "PLAINTEXT";

    /**
     * AWS_MSK_IAM
     */
    private String saslMechanism;

    /**
     * software.amazon.msk.auth.iam.IAMLoginModule required;
     */
    private String saslJaasConfig;

    /**
     * software.amazon.msk.auth.iam.IAMClientCallbackHandler
     */
    private String saslClientCallbackHandlerClass;

    /**
     * Topic 설정
     */
    private Topics topics =
            new Topics();

    @Getter
    @Setter
    public static class Topics {

        private Topic raw =
                new Topic(
                        "factory.manufacturing.raw",
                        4
                );

        private Topic analysis =
                new Topic(
                        "factory.manufacturing.analysis",
                        2
                );

        private Topic alert =
                new Topic(
                        "factory.manufacturing.alert",
                        2
                );

        private Topic equipment =
                new Topic(
                        "factory.manufacturing.equipment",
                        2
                );
    }

    @Getter
    @Setter
    public static class Topic {

        private String name;

        private int partitions;

        public Topic() {
        }

        public Topic(
                String name,
                int partitions
        ) {
            this.name = name;
            this.partitions = partitions;
        }
    }
}