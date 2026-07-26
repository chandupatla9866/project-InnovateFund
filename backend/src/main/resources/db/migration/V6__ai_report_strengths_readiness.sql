ALTER TABLE public.ai_reports
    ADD COLUMN strengths_json text,
    ADD COLUMN investor_readiness_status varchar(100),
    ADD COLUMN investor_readiness_confidence varchar(20);
