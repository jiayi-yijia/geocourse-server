package com.bddk.geocourse.module.architecture.model;

import java.util.List;

public record ArchitectureOverview(
        String projectName,
        String projectVersion,
        String architectureStyle,
        String deploymentMode,
        String tenantStrategy,
        List<String> activeProfiles,
        List<ArchitectureModule> modules,
        List<InfrastructureNode> infrastructure,
        List<DeliveryPhase> roadmap
) {

    public record ArchitectureModule(
            String code,
            String name,
            String responsibility,
            List<String> packagePlan,
            List<String> capabilities,
            List<String> integrations
    ) {
    }

    public record InfrastructureNode(
            String name,
            String endpoint,
            String purpose,
            String note
    ) {
    }

    public record DeliveryPhase(
            Integer phase,
            String stage,
            List<String> scope
    ) {
    }

}

