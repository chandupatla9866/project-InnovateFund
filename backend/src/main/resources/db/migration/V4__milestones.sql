CREATE TABLE public.milestones (
    id uuid NOT NULL,
    startup_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    target_date date,
    completed boolean NOT NULL DEFAULT false,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT milestones_pkey PRIMARY KEY (id),
    CONSTRAINT fk_milestones_startup FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE
);

CREATE INDEX idx_milestones_startup_id ON public.milestones USING btree (startup_id);
