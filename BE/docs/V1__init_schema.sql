--
-- PostgreSQL database dump
--

\restrict acd5uAHhQXVSPO6LIbHrC3ZiOQVH4TGWNSq2HWH3dfTaaekhhnMKw3zTumbqCjY

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-05-06 13:42:55

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

--
-- TOC entry 2 (class 3079 OID 17683)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5211 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 223 (class 1259 OID 17790)
-- Name: card_attacks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_attacks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    card_id character varying(80) NOT NULL,
    attack_index integer NOT NULL,
    name character varying(160) NOT NULL,
    printed_cost text[] DEFAULT '{}'::text[] NOT NULL,
    converted_energy_cost integer DEFAULT 0 NOT NULL,
    damage_text character varying(40),
    base_damage integer,
    effect_text text,
    effect_code character varying(80),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.card_attacks OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 17831)
-- Name: card_resistances; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_resistances (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    card_id character varying(80) NOT NULL,
    energy_type character varying(30) NOT NULL,
    value integer DEFAULT '-20'::integer NOT NULL
);


ALTER TABLE public.card_resistances OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 17815)
-- Name: card_weaknesses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card_weaknesses (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    card_id character varying(80) NOT NULL,
    energy_type character varying(30) NOT NULL,
    multiplier integer DEFAULT 2 NOT NULL
);


ALTER TABLE public.card_weaknesses OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 17753)
-- Name: cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cards (
    id character varying(80) NOT NULL,
    name character varying(160) NOT NULL,
    supertype character varying(30) NOT NULL,
    subtypes text[] DEFAULT '{}'::text[] NOT NULL,
    set_code character varying(30) NOT NULL,
    number character varying(30),
    rarity character varying(80),
    image_small_url text,
    image_large_url text,
    hp integer,
    pokemon_stage character varying(30),
    evolves_from character varying(160),
    pokemon_types text[] DEFAULT '{}'::text[] NOT NULL,
    retreat_cost text[] DEFAULT '{}'::text[] NOT NULL,
    converted_retreat_cost integer,
    is_ex boolean DEFAULT false NOT NULL,
    is_mega boolean DEFAULT false NOT NULL,
    energy_card_type character varying(30),
    provides_energy_types text[] DEFAULT '{}'::text[] NOT NULL,
    trainer_subtype character varying(30),
    is_ace_spec boolean DEFAULT false NOT NULL,
    rules_text text[] DEFAULT '{}'::text[] NOT NULL,
    raw_json jsonb NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.cards OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 17874)
-- Name: deck_cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.deck_cards (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    deck_id uuid NOT NULL,
    card_id character varying(80) NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT deck_cards_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.deck_cards OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 17847)
-- Name: decks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.decks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    owner_user_id uuid,
    name character varying(120) NOT NULL,
    source character varying(30) DEFAULT 'USER'::character varying NOT NULL,
    valid boolean DEFAULT false NOT NULL,
    validation_errors jsonb DEFAULT '[]'::jsonb NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.decks OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 17743)
-- Name: guest_players; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.guest_players (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    display_name character varying(80) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.guest_players OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 17968)
-- Name: match_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.match_logs (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    match_id uuid NOT NULL,
    version bigint NOT NULL,
    turn_number integer NOT NULL,
    player_id uuid,
    action_type character varying(80),
    event_type character varying(80) NOT NULL,
    result character varying(30) NOT NULL,
    message text,
    payload jsonb DEFAULT '{}'::jsonb NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.match_logs OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 17915)
-- Name: match_players; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.match_players (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    match_id uuid NOT NULL,
    player_id uuid NOT NULL,
    player_kind character varying(20) NOT NULL,
    side character varying(30) NOT NULL,
    deck_id uuid,
    display_name character varying(80) NOT NULL,
    joined_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.match_players OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 17945)
-- Name: match_states; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.match_states (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    match_id uuid NOT NULL,
    version bigint NOT NULL,
    serialized_state jsonb NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.match_states OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 17899)
-- Name: matches; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.matches (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    status character varying(30) NOT NULL,
    current_phase character varying(30),
    turn_number integer DEFAULT 0 NOT NULL,
    current_player_id uuid,
    first_player_id uuid,
    winner_player_id uuid,
    finish_reason character varying(60),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    finished_at timestamp without time zone,
    latest_state_version bigint
);


ALTER TABLE public.matches OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 17721)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(120),
    password_hash text,
    display_name character varying(80),
    role character varying(30) DEFAULT 'PLAYER'::character varying NOT NULL,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 5197 (class 0 OID 17790)
-- Dependencies: 223
-- Data for Name: card_attacks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.card_attacks (id, card_id, attack_index, name, printed_cost, converted_energy_cost, damage_text, base_damage, effect_text, effect_code, created_at) FROM stdin;
\.


--
-- TOC entry 5199 (class 0 OID 17831)
-- Dependencies: 225
-- Data for Name: card_resistances; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.card_resistances (id, card_id, energy_type, value) FROM stdin;
\.


--
-- TOC entry 5198 (class 0 OID 17815)
-- Dependencies: 224
-- Data for Name: card_weaknesses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.card_weaknesses (id, card_id, energy_type, multiplier) FROM stdin;
\.


--
-- TOC entry 5196 (class 0 OID 17753)
-- Dependencies: 222
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cards (id, name, supertype, subtypes, set_code, number, rarity, image_small_url, image_large_url, hp, pokemon_stage, evolves_from, pokemon_types, retreat_cost, converted_retreat_cost, is_ex, is_mega, energy_card_type, provides_energy_types, trainer_subtype, is_ace_spec, rules_text, raw_json, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5201 (class 0 OID 17874)
-- Dependencies: 227
-- Data for Name: deck_cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.deck_cards (id, deck_id, card_id, quantity, created_at) FROM stdin;
\.


--
-- TOC entry 5200 (class 0 OID 17847)
-- Dependencies: 226
-- Data for Name: decks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.decks (id, owner_user_id, name, source, valid, validation_errors, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5195 (class 0 OID 17743)
-- Dependencies: 221
-- Data for Name: guest_players; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.guest_players (id, display_name, created_at) FROM stdin;
\.


--
-- TOC entry 5205 (class 0 OID 17968)
-- Dependencies: 231
-- Data for Name: match_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.match_logs (id, match_id, version, turn_number, player_id, action_type, event_type, result, message, payload, created_at) FROM stdin;
\.


--
-- TOC entry 5203 (class 0 OID 17915)
-- Dependencies: 229
-- Data for Name: match_players; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.match_players (id, match_id, player_id, player_kind, side, deck_id, display_name, joined_at) FROM stdin;
\.


--
-- TOC entry 5204 (class 0 OID 17945)
-- Dependencies: 230
-- Data for Name: match_states; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.match_states (id, match_id, version, serialized_state, created_at) FROM stdin;
\.


--
-- TOC entry 5202 (class 0 OID 17899)
-- Dependencies: 228
-- Data for Name: matches; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.matches (id, status, current_phase, turn_number, current_player_id, first_player_id, winner_player_id, finish_reason, created_at, updated_at, finished_at, latest_state_version) FROM stdin;
\.


--
-- TOC entry 5194 (class 0 OID 17721)
-- Dependencies: 220
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, email, password_hash, display_name, role, status, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 4998 (class 2606 OID 17809)
-- Name: card_attacks card_attacks_card_id_attack_index_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_attacks
    ADD CONSTRAINT card_attacks_card_id_attack_index_key UNIQUE (card_id, attack_index);


--
-- TOC entry 5000 (class 2606 OID 17807)
-- Name: card_attacks card_attacks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_attacks
    ADD CONSTRAINT card_attacks_pkey PRIMARY KEY (id);


--
-- TOC entry 5004 (class 2606 OID 17841)
-- Name: card_resistances card_resistances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_resistances
    ADD CONSTRAINT card_resistances_pkey PRIMARY KEY (id);


--
-- TOC entry 5002 (class 2606 OID 17825)
-- Name: card_weaknesses card_weaknesses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_weaknesses
    ADD CONSTRAINT card_weaknesses_pkey PRIMARY KEY (id);


--
-- TOC entry 4991 (class 2606 OID 17784)
-- Name: cards cards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_pkey PRIMARY KEY (id);


--
-- TOC entry 5010 (class 2606 OID 17888)
-- Name: deck_cards deck_cards_deck_id_card_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deck_cards
    ADD CONSTRAINT deck_cards_deck_id_card_id_key UNIQUE (deck_id, card_id);


--
-- TOC entry 5012 (class 2606 OID 17886)
-- Name: deck_cards deck_cards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deck_cards
    ADD CONSTRAINT deck_cards_pkey PRIMARY KEY (id);


--
-- TOC entry 5006 (class 2606 OID 17866)
-- Name: decks decks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.decks
    ADD CONSTRAINT decks_pkey PRIMARY KEY (id);


--
-- TOC entry 4989 (class 2606 OID 17752)
-- Name: guest_players guest_players_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.guest_players
    ADD CONSTRAINT guest_players_pkey PRIMARY KEY (id);


--
-- TOC entry 5036 (class 2606 OID 17985)
-- Name: match_logs match_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_logs
    ADD CONSTRAINT match_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 5020 (class 2606 OID 17932)
-- Name: match_players match_players_match_id_player_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_players
    ADD CONSTRAINT match_players_match_id_player_id_key UNIQUE (match_id, player_id);


--
-- TOC entry 5022 (class 2606 OID 17930)
-- Name: match_players match_players_match_id_side_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_players
    ADD CONSTRAINT match_players_match_id_side_key UNIQUE (match_id, side);


--
-- TOC entry 5024 (class 2606 OID 17928)
-- Name: match_players match_players_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_players
    ADD CONSTRAINT match_players_pkey PRIMARY KEY (id);


--
-- TOC entry 5028 (class 2606 OID 17960)
-- Name: match_states match_states_match_id_version_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_states
    ADD CONSTRAINT match_states_match_id_version_key UNIQUE (match_id, version);


--
-- TOC entry 5030 (class 2606 OID 17958)
-- Name: match_states match_states_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_states
    ADD CONSTRAINT match_states_pkey PRIMARY KEY (id);


--
-- TOC entry 5016 (class 2606 OID 17912)
-- Name: matches matches_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT matches_pkey PRIMARY KEY (id);


--
-- TOC entry 4983 (class 2606 OID 17742)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 4985 (class 2606 OID 17738)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4987 (class 2606 OID 17740)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4992 (class 1259 OID 17789)
-- Name: idx_cards_is_ace_spec; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cards_is_ace_spec ON public.cards USING btree (is_ace_spec);


--
-- TOC entry 4993 (class 1259 OID 17785)
-- Name: idx_cards_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cards_name ON public.cards USING btree (name);


--
-- TOC entry 4994 (class 1259 OID 17786)
-- Name: idx_cards_set_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cards_set_code ON public.cards USING btree (set_code);


--
-- TOC entry 4995 (class 1259 OID 17787)
-- Name: idx_cards_supertype; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cards_supertype ON public.cards USING btree (supertype);


--
-- TOC entry 4996 (class 1259 OID 17788)
-- Name: idx_cards_trainer_subtype; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cards_trainer_subtype ON public.cards USING btree (trainer_subtype);


--
-- TOC entry 5007 (class 1259 OID 17872)
-- Name: idx_decks_owner_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_decks_owner_user_id ON public.decks USING btree (owner_user_id);


--
-- TOC entry 5008 (class 1259 OID 17873)
-- Name: idx_decks_source; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_decks_source ON public.decks USING btree (source);


--
-- TOC entry 5031 (class 1259 OID 17993)
-- Name: idx_match_logs_event_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_logs_event_type ON public.match_logs USING btree (event_type);


--
-- TOC entry 5032 (class 1259 OID 17992)
-- Name: idx_match_logs_match_turn; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_logs_match_turn ON public.match_logs USING btree (match_id, turn_number);


--
-- TOC entry 5033 (class 1259 OID 17991)
-- Name: idx_match_logs_match_version; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_logs_match_version ON public.match_logs USING btree (match_id, version);


--
-- TOC entry 5034 (class 1259 OID 17994)
-- Name: idx_match_logs_payload; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_logs_payload ON public.match_logs USING gin (payload);


--
-- TOC entry 5017 (class 1259 OID 17943)
-- Name: idx_match_players_match_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_players_match_id ON public.match_players USING btree (match_id);


--
-- TOC entry 5018 (class 1259 OID 17944)
-- Name: idx_match_players_player_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_players_player_id ON public.match_players USING btree (player_id);


--
-- TOC entry 5025 (class 1259 OID 17967)
-- Name: idx_match_states_json; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_states_json ON public.match_states USING gin (serialized_state);


--
-- TOC entry 5026 (class 1259 OID 17966)
-- Name: idx_match_states_match_version; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_match_states_match_version ON public.match_states USING btree (match_id, version DESC);


--
-- TOC entry 5013 (class 1259 OID 17914)
-- Name: idx_matches_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_matches_created_at ON public.matches USING btree (created_at);


--
-- TOC entry 5014 (class 1259 OID 17913)
-- Name: idx_matches_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_matches_status ON public.matches USING btree (status);


--
-- TOC entry 5037 (class 2606 OID 17810)
-- Name: card_attacks card_attacks_card_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_attacks
    ADD CONSTRAINT card_attacks_card_id_fkey FOREIGN KEY (card_id) REFERENCES public.cards(id) ON DELETE CASCADE;


--
-- TOC entry 5039 (class 2606 OID 17842)
-- Name: card_resistances card_resistances_card_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_resistances
    ADD CONSTRAINT card_resistances_card_id_fkey FOREIGN KEY (card_id) REFERENCES public.cards(id) ON DELETE CASCADE;


--
-- TOC entry 5038 (class 2606 OID 17826)
-- Name: card_weaknesses card_weaknesses_card_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card_weaknesses
    ADD CONSTRAINT card_weaknesses_card_id_fkey FOREIGN KEY (card_id) REFERENCES public.cards(id) ON DELETE CASCADE;


--
-- TOC entry 5041 (class 2606 OID 17894)
-- Name: deck_cards deck_cards_card_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deck_cards
    ADD CONSTRAINT deck_cards_card_id_fkey FOREIGN KEY (card_id) REFERENCES public.cards(id);


--
-- TOC entry 5042 (class 2606 OID 17889)
-- Name: deck_cards deck_cards_deck_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deck_cards
    ADD CONSTRAINT deck_cards_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES public.decks(id) ON DELETE CASCADE;


--
-- TOC entry 5040 (class 2606 OID 17867)
-- Name: decks decks_owner_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.decks
    ADD CONSTRAINT decks_owner_user_id_fkey FOREIGN KEY (owner_user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- TOC entry 5046 (class 2606 OID 17986)
-- Name: match_logs match_logs_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_logs
    ADD CONSTRAINT match_logs_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.matches(id) ON DELETE CASCADE;


--
-- TOC entry 5043 (class 2606 OID 17938)
-- Name: match_players match_players_deck_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_players
    ADD CONSTRAINT match_players_deck_id_fkey FOREIGN KEY (deck_id) REFERENCES public.decks(id);


--
-- TOC entry 5044 (class 2606 OID 17933)
-- Name: match_players match_players_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_players
    ADD CONSTRAINT match_players_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.matches(id) ON DELETE CASCADE;


--
-- TOC entry 5045 (class 2606 OID 17961)
-- Name: match_states match_states_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.match_states
    ADD CONSTRAINT match_states_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.matches(id) ON DELETE CASCADE;


-- Completed on 2026-05-06 13:42:55

--
-- PostgreSQL database dump complete
--

\unrestrict acd5uAHhQXVSPO6LIbHrC3ZiOQVH4TGWNSq2HWH3dfTaaekhhnMKw3zTumbqCjY

