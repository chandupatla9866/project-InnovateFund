ALTER TABLE public.meetings ADD COLUMN meeting_link varchar(500);

ALTER TABLE public.startups
    ADD COLUMN equity_offered numeric(5,2),
    ADD COLUMN website_url varchar(255),
    ADD COLUMN social_links text;

CREATE TABLE public.team_members (
    id uuid NOT NULL,
    startup_id uuid NOT NULL,
    name varchar(255) NOT NULL,
    role varchar(255),
    bio text,
    photo_url varchar(255),
    display_order integer NOT NULL DEFAULT 0,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT team_members_pkey PRIMARY KEY (id),
    CONSTRAINT fk_team_members_startup FOREIGN KEY (startup_id) REFERENCES public.startups(id) ON DELETE CASCADE
);

CREATE INDEX idx_team_members_startup_id ON public.team_members USING btree (startup_id);
