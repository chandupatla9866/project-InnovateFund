package com.innovfund.ai.pitch;

import com.innovfund.startup.entity.Startup;

public interface PitchImprovementService {
    PitchImprovementResult improve(Startup startup);
}
