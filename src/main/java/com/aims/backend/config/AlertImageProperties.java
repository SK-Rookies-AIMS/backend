package com.aims.backend.config;

import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.alert.image")
public class AlertImageProperties {

    private Map<ProcessCode, ImageSet> processImages = new EnumMap<>(ProcessCode.class);

    /** processCode + alertType + severity 조합으로 S3 이미지 URL 결정 */
    public String resolve(ProcessCode processCode, AlertType alertType, AlertSeverity severity) {

        ImageSet imageSet = processImages.get(processCode);
        if (imageSet == null || severity == null) {
            return null;
        }

        if (severity == AlertSeverity.CAUTION) {
            return imageSet.getWarning(); // 설비/공정 관계없이 동일 이미지
        }

        return alertType == AlertType.EQUIPMENT
                ? imageSet.getDangerEquipment()
                : imageSet.getDangerProcess();
    }

    @Getter
    @Setter
    public static class ImageSet {
        private String dangerEquipment;
        private String dangerProcess;
        private String warning;
    }
}