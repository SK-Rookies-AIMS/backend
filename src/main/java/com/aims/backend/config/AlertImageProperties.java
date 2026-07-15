package com.aims.backend.config;

import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.alert.image")
public class AlertImageProperties {

    private Map<ProcessCode, ImageSet> processImages = new EnumMap<>(ProcessCode.class);

    @PostConstruct
    public void logLoadedImages() {
        log.info("AlertImageProperties loaded. size={}, keys={}",
                processImages.size(), processImages.keySet());
        processImages.forEach((code, set) ->
                log.info("  {} -> dangerEquipment={}, dangerProcess={}, warning={}",
                        code, set.getDangerEquipment(), set.getDangerProcess(), set.getWarning()));
    }

    public String resolve(ProcessCode processCode, AlertType alertType, AlertSeverity severity) {

        ImageSet imageSet = processImages.get(processCode);

        log.info("resolve() called. processCode={}, alertType={}, severity={}, imageSetFound={}",
                processCode, alertType, severity, imageSet != null);

        if (imageSet == null || severity == null) {
            return null;
        }

        if (severity == AlertSeverity.CAUTION) {
            return imageSet.getWarning();
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