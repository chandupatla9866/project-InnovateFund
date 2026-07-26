package com.innovfund.ai.mentor;

import com.innovfund.startup.entity.Startup;

public interface MentorService {
    MentorAnswer answer(Startup startup, String question);
}
