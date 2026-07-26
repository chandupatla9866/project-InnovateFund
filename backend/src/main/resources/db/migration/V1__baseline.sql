--
-- PostgreSQL database dump
--


-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ai_reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_reports (
    id uuid NOT NULL,
    category_scores_json text NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    model_version character varying(255) NOT NULL,
    overall_score double precision NOT NULL,
    suggestions_json text NOT NULL,
    summary_text text,
    startup_id uuid NOT NULL
);


--
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_messages (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    read boolean NOT NULL,
    text text NOT NULL,
    recipient_id uuid NOT NULL,
    sender_id uuid NOT NULL
);


--
-- Name: comments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.comments (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    text text NOT NULL,
    author_id uuid NOT NULL,
    post_id uuid NOT NULL
);


--
-- Name: due_diligence_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.due_diligence_documents (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    document_type character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    startup_id uuid NOT NULL,
    uploaded_by_id uuid NOT NULL,
    CONSTRAINT due_diligence_documents_document_type_check CHECK (((document_type)::text = ANY ((ARRAY['FINANCIAL_REPORT'::character varying, 'REVENUE'::character varying, 'CERTIFICATE'::character varying, 'PATENT'::character varying, 'LEGAL'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: due_diligence_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.due_diligence_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    responded_at timestamp(6) with time zone,
    status character varying(255) NOT NULL,
    investor_id uuid NOT NULL,
    startup_id uuid NOT NULL,
    CONSTRAINT due_diligence_requests_status_check CHECK (((status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.events (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    event_date timestamp(6) with time zone NOT NULL,
    link character varying(255),
    location character varying(255),
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    created_by_id uuid NOT NULL,
    CONSTRAINT events_type_check CHECK (((type)::text = ANY ((ARRAY['EVENT'::character varying, 'COMPETITION'::character varying])::text[])))
);


--
-- Name: follows; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.follows (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    investor_id uuid NOT NULL,
    startup_id uuid NOT NULL
);


--
-- Name: founder_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.founder_profiles (
    id uuid NOT NULL,
    bio text,
    created_at timestamp(6) with time zone NOT NULL,
    full_name character varying(255) NOT NULL,
    linkedin_url character varying(255),
    phone character varying(255),
    verified boolean NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: investments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.investments (
    id uuid NOT NULL,
    amount numeric(15,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    notes text,
    investor_id uuid NOT NULL,
    startup_id uuid NOT NULL
);


--
-- Name: investor_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.investor_profiles (
    id uuid NOT NULL,
    bio text,
    created_at timestamp(6) with time zone NOT NULL,
    firm_name character varying(255),
    full_name character varying(255) NOT NULL,
    investment_interests text,
    verified boolean NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: likes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.likes (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    post_id uuid NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: meetings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.meetings (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    duration_minutes integer NOT NULL,
    notes text,
    scheduled_at timestamp(6) with time zone NOT NULL,
    status character varying(255) NOT NULL,
    recipient_id uuid NOT NULL,
    requester_id uuid NOT NULL,
    startup_id uuid,
    summary_json text,
    transcript text,
    CONSTRAINT meetings_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    link character varying(255),
    message character varying(255) NOT NULL,
    read boolean NOT NULL,
    type character varying(255) NOT NULL,
    recipient_id uuid NOT NULL
);


--
-- Name: posts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.posts (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    media_url character varying(255),
    text text NOT NULL,
    type character varying(255) NOT NULL,
    author_id uuid NOT NULL,
    startup_id uuid,
    CONSTRAINT posts_type_check CHECK (((type)::text = ANY ((ARRAY['PRODUCT_LAUNCH'::character varying, 'MILESTONE'::character varying, 'FUNDING_UPDATE'::character varying, 'ACHIEVEMENT'::character varying, 'HIRING'::character varying, 'ANNOUNCEMENT'::character varying, 'INVESTMENT_OPPORTUNITY'::character varying, 'MARKET_INSIGHT'::character varying, 'STARTUP_TIPS'::character varying, 'GENERAL'::character varying])::text[])))
);


--
-- Name: reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reports (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    reason text NOT NULL,
    status character varying(50) NOT NULL,
    target_id uuid NOT NULL,
    target_type character varying(50) NOT NULL,
    reporter_id uuid NOT NULL,
    CONSTRAINT reports_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('RESOLVED'::character varying)::text, ('DISMISSED'::character varying)::text]))),
    CONSTRAINT reports_target_type_check CHECK (((target_type)::text = ANY (ARRAY[('POST'::character varying)::text, ('STARTUP'::character varying)::text, ('USER'::character varying)::text])))
);


--
-- Name: startup_interests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.startup_interests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    investor_id uuid NOT NULL,
    startup_id uuid NOT NULL
);


--
-- Name: startups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.startups (
    id uuid NOT NULL,
    business_model text,
    competitors text,
    cover_image_url character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    demo_video_url character varying(255),
    funding_goal numeric(15,2),
    funding_progress numeric(15,2),
    industry character varying(255),
    logo_url character varying(255),
    market text,
    name character varying(255) NOT NULL,
    pitch_deck_url character varying(255),
    problem text,
    published boolean NOT NULL,
    revenue_model text,
    solution text,
    stage character varying(255),
    target_audience text,
    updated_at timestamp(6) with time zone NOT NULL,
    verified boolean NOT NULL,
    founder_id uuid NOT NULL,
    CONSTRAINT startups_stage_check CHECK (((stage)::text = ANY ((ARRAY['IDEA'::character varying, 'MVP'::character varying, 'EARLY_TRACTION'::character varying, 'GROWTH'::character varying, 'SCALING'::character varying])::text[])))
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    enabled boolean NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['FOUNDER'::character varying, 'INVESTOR'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- Name: ai_reports ai_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_reports
    ADD CONSTRAINT ai_reports_pkey PRIMARY KEY (id);


--
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- Name: comments comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: due_diligence_documents due_diligence_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_documents
    ADD CONSTRAINT due_diligence_documents_pkey PRIMARY KEY (id);


--
-- Name: due_diligence_requests due_diligence_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_requests
    ADD CONSTRAINT due_diligence_requests_pkey PRIMARY KEY (id);


--
-- Name: events events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: follows follows_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT follows_pkey PRIMARY KEY (id);


--
-- Name: founder_profiles founder_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.founder_profiles
    ADD CONSTRAINT founder_profiles_pkey PRIMARY KEY (id);


--
-- Name: investments investments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investments
    ADD CONSTRAINT investments_pkey PRIMARY KEY (id);


--
-- Name: investor_profiles investor_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investor_profiles
    ADD CONSTRAINT investor_profiles_pkey PRIMARY KEY (id);


--
-- Name: likes likes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_pkey PRIMARY KEY (id);


--
-- Name: meetings meetings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT meetings_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: posts posts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (id);


--
-- Name: reports reports_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);


--
-- Name: startup_interests startup_interests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startup_interests
    ADD CONSTRAINT startup_interests_pkey PRIMARY KEY (id);


--
-- Name: startups startups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startups
    ADD CONSTRAINT startups_pkey PRIMARY KEY (id);


--
-- Name: founder_profiles uk2ajgog5lr6hx6qs4owcp6uivi; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.founder_profiles
    ADD CONSTRAINT uk2ajgog5lr6hx6qs4owcp6uivi UNIQUE (user_id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: investor_profiles uksoqxl69ixbqvymc4ig76oawej; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investor_profiles
    ADD CONSTRAINT uksoqxl69ixbqvymc4ig76oawej UNIQUE (user_id);


--
-- Name: due_diligence_requests uq_dd_investor_startup; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_requests
    ADD CONSTRAINT uq_dd_investor_startup UNIQUE (investor_id, startup_id);


--
-- Name: follows uq_follow_investor_startup; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT uq_follow_investor_startup UNIQUE (investor_id, startup_id);


--
-- Name: startup_interests uq_interest_investor_startup; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startup_interests
    ADD CONSTRAINT uq_interest_investor_startup UNIQUE (investor_id, startup_id);


--
-- Name: likes uq_like_post_user; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT uq_like_post_user UNIQUE (post_id, user_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_ai_reports_startup_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_reports_startup_id ON public.ai_reports USING btree (startup_id);


--
-- Name: idx_chat_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_created_at ON public.chat_messages USING btree (created_at);


--
-- Name: idx_chat_sender_recipient; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_sender_recipient ON public.chat_messages USING btree (sender_id, recipient_id);


--
-- Name: idx_dd_docs_startup_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dd_docs_startup_id ON public.due_diligence_documents USING btree (startup_id);


--
-- Name: idx_events_event_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_events_event_date ON public.events USING btree (event_date);


--
-- Name: idx_investments_investor_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_investments_investor_id ON public.investments USING btree (investor_id);


--
-- Name: idx_investments_startup_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_investments_startup_id ON public.investments USING btree (startup_id);


--
-- Name: idx_meetings_recipient_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_meetings_recipient_id ON public.meetings USING btree (recipient_id);


--
-- Name: idx_meetings_requester_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_meetings_requester_id ON public.meetings USING btree (requester_id);


--
-- Name: idx_notifications_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_created_at ON public.notifications USING btree (created_at);


--
-- Name: idx_notifications_recipient_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_recipient_id ON public.notifications USING btree (recipient_id);


--
-- Name: idx_posts_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_posts_created_at ON public.posts USING btree (created_at);


--
-- Name: idx_posts_startup_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_posts_startup_id ON public.posts USING btree (startup_id);


--
-- Name: idx_reports_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reports_status ON public.reports USING btree (status);


--
-- Name: idx_startups_founder_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_startups_founder_id ON public.startups USING btree (founder_id);


--
-- Name: idx_startups_published; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_startups_published ON public.startups USING btree (published);


--
-- Name: investments fk5vub0peihxejn4a0dp58iwc6c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investments
    ADD CONSTRAINT fk5vub0peihxejn4a0dp58iwc6c FOREIGN KEY (investor_id) REFERENCES public.users(id);


--
-- Name: founder_profiles fk6jlngavc1m1f123c5jna446h0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.founder_profiles
    ADD CONSTRAINT fk6jlngavc1m1f123c5jna446h0 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: posts fk6xvn0811tkyo3nfjk2xvqx6ns; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fk6xvn0811tkyo3nfjk2xvqx6ns FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: meetings fk750tklmb3b1len131tumwane9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT fk750tklmb3b1len131tumwane9 FOREIGN KEY (requester_id) REFERENCES public.users(id);


--
-- Name: due_diligence_documents fk8hx2ltkh4vxlmq1010l9ejxv9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_documents
    ADD CONSTRAINT fk8hx2ltkh4vxlmq1010l9ejxv9 FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: posts fk95wghbflnvrr1ajd4ol66h4ms; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fk95wghbflnvrr1ajd4ol66h4ms FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: chat_messages fk9cy5qdbo924k3jflvj0y04s6y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fk9cy5qdbo924k3jflvj0y04s6y FOREIGN KEY (recipient_id) REFERENCES public.users(id);


--
-- Name: investments fk9pmv220qlxmk8bxstqju7t1vy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investments
    ADD CONSTRAINT fk9pmv220qlxmk8bxstqju7t1vy FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: events fkbd320pnltw8sobixakxi103t2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT fkbd320pnltw8sobixakxi103t2 FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: investor_profiles fkbpfclc3evlln2gajncfevq8k7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.investor_profiles
    ADD CONSTRAINT fkbpfclc3evlln2gajncfevq8k7 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reports fkd3qiw2om5d2oh5xb7fbdcq225; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT fkd3qiw2om5d2oh5xb7fbdcq225 FOREIGN KEY (reporter_id) REFERENCES public.users(id);


--
-- Name: follows fkdmjc3sh87ntkp7f8cm3j78yvj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT fkdmjc3sh87ntkp7f8cm3j78yvj FOREIGN KEY (investor_id) REFERENCES public.users(id);


--
-- Name: startup_interests fkdvofbhk81oplvcd6ib34kn86n; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startup_interests
    ADD CONSTRAINT fkdvofbhk81oplvcd6ib34kn86n FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: due_diligence_documents fkgbp6qr3i5iuoy6fnrbvy0o2ff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_documents
    ADD CONSTRAINT fkgbp6qr3i5iuoy6fnrbvy0o2ff FOREIGN KEY (uploaded_by_id) REFERENCES public.users(id);


--
-- Name: chat_messages fkgiqeap8ays4lf684x7m0r2729; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fkgiqeap8ays4lf684x7m0r2729 FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: comments fkh4c7lvsc298whoyd4w9ta25cr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fkh4c7lvsc298whoyd4w9ta25cr FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: startup_interests fkhbaddk1pp75xp162c0kaomitp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startup_interests
    ADD CONSTRAINT fkhbaddk1pp75xp162c0kaomitp FOREIGN KEY (investor_id) REFERENCES public.users(id);


--
-- Name: due_diligence_requests fkjvd1ki9ouupixaa1xddk002yy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_requests
    ADD CONSTRAINT fkjvd1ki9ouupixaa1xddk002yy FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: comments fkn2na60ukhs76ibtpt9burkm27; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fkn2na60ukhs76ibtpt9burkm27 FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: follows fknt3kasbivf5tmfmp5yw27i2xl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follows
    ADD CONSTRAINT fknt3kasbivf5tmfmp5yw27i2xl FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: likes fknvx9seeqqyy71bij291pwiwrg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fknvx9seeqqyy71bij291pwiwrg FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: meetings fkotwvjdacds78uc62y3ho9t00c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT fkotwvjdacds78uc62y3ho9t00c FOREIGN KEY (recipient_id) REFERENCES public.users(id);


--
-- Name: startups fkpvsm0wjj3kuk4b3n1il42gcwy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.startups
    ADD CONSTRAINT fkpvsm0wjj3kuk4b3n1il42gcwy FOREIGN KEY (founder_id) REFERENCES public.users(id);


--
-- Name: ai_reports fkqgx8uj7d0xo7lqkx14ymtlmm5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_reports
    ADD CONSTRAINT fkqgx8uj7d0xo7lqkx14ymtlmm5 FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- Name: notifications fkqqnsjxlwleyjbxlmm213jaj3f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkqqnsjxlwleyjbxlmm213jaj3f FOREIGN KEY (recipient_id) REFERENCES public.users(id);


--
-- Name: likes fkry8tnr4x2vwemv2bb0h5hyl0x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fkry8tnr4x2vwemv2bb0h5hyl0x FOREIGN KEY (post_id) REFERENCES public.posts(id);


--
-- Name: due_diligence_requests fkseqsbgpttqd0fjp90lbgv0iib; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.due_diligence_requests
    ADD CONSTRAINT fkseqsbgpttqd0fjp90lbgv0iib FOREIGN KEY (investor_id) REFERENCES public.users(id);


--
-- Name: meetings fkt5scbte6s2htui1wr5xqygp9p; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.meetings
    ADD CONSTRAINT fkt5scbte6s2htui1wr5xqygp9p FOREIGN KEY (startup_id) REFERENCES public.startups(id);


--
-- PostgreSQL database dump complete
--


