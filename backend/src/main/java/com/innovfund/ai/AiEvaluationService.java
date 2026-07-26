package com.innovfund.ai;

import com.innovfund.startup.entity.Startup;

public interface AiEvaluationService {
    AiEvaluationResult evaluate(Startup startup);
}
