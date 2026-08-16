-- A DELIBERATELY WEAKENED COPY of init.sql. Not used by any normal run.
--
-- Every constraint a negative scenario asserts on is removed here. Booting the suite against
-- this schema MUST make it fail: the scenario asserting 409 on a duplicate email has had the
-- unique index taken away, the one asserting 400 on negative stock has lost its CHECK, and so on.
--
-- A negative test that cannot fail is not a test. This file is how CI proves these ones can —
-- see the "Prove the negative tests can fail" job, which runs the suite against this schema and
-- treats a green result as the failure.
--
-- The exact set of scenarios that must go red is in mutation-expectations.txt, and the job
-- compares against it rather than settling for "something failed". Removing a weakening here
-- without removing the line there is a build failure, which is the only reason the two files
-- stay in step.
--
-- Keep in step with init.sql too. Add a constraint there and a scenario asserting it, and weaken
-- it here, or the mutation job silently stops covering it.

-- Schema for the containerised API under test.
--
-- PostgREST exposes one Postgres schema as a REST API, so this file *is* the API
-- definition: the tables become endpoints, and the constraints become the 400s and
-- 409s the negative tests assert on. That is the point of the exercise — the error
-- responses come from a real database rejecting real bad data, not from a mock that
-- was told to return a number.
--
-- Column names are quoted camelCase ("lastName", not last_name). Unidiomatic for
-- Postgres, deliberate here: PostgREST exposes column names verbatim, and quoting
-- them keeps the JSON contract identical to what the existing models, feature files
-- and JSON schemas already speak.

CREATE SCHEMA IF NOT EXISTS api;

-- ---------------------------------------------------------------- clients

CREATE TABLE api.clients (
    id         serial PRIMARY KEY,
    -- NOT NULL removed. "Creating a client without a required field is rejected" asserts 23502,
    -- which is precisely this constraint firing; with it present that scenario passed under
    -- mutation, meaning nothing proved it could fail.
    "name"     text CHECK (length(btrim("name")) > 0),
    "lastName" text NOT NULL CHECK (length(btrim("lastName")) > 0),
    country    text NOT NULL,
    city       text NOT NULL,
    phone      text NOT NULL,
    email      text NOT NULL  -- UNIQUE and CHECK deliberately removed
);

-- --------------------------------------------------------------- resources

CREATE TABLE api.resources (
    id          serial PRIMARY KEY,
    "name"      text NOT NULL,  -- CHECK deliberately removed: "a blank name is rejected"
    trademark   text NOT NULL,
    -- Non-negative stock and price are the constraints most worth asserting on:
    -- they are the ones a careless client actually violates.
    stock       integer NOT NULL,  -- CHECK deliberately removed
    price       numeric(10, 2) NOT NULL,  -- CHECK deliberately removed
    description text NOT NULL,
    tags        text NOT NULL DEFAULT '',
    active      boolean NOT NULL DEFAULT true
);

-- ------------------------------------------------------------------- seed
--
-- Enough rows for the "there are at least N" preconditions to hold on a cold
-- container, so no scenario has to create data just to satisfy a Background.
-- Every run starts from exactly this state, which is the whole point of moving
-- off the shared mock.

INSERT INTO api.clients ("name", "lastName", country, city, phone, email) VALUES
    ('Diego',  'Navarro',  'Colombia', 'Medellin',  '3003391905', 'diego.navarro@example.com'),
    ('Laura',  'Ospina',   'Colombia', 'Medellin',  '3111111111', 'laura.ospina@example.com'),
    ('Camila', 'Restrepo', 'Colombia', 'Bogota',    '3122222222', 'camila.restrepo@example.com'),
    ('Andres', 'Gomez',    'Colombia', 'Cali',      '3133333333', 'andres.gomez@example.com'),
    ('Sofia',  'Marin',    'Colombia', 'Barranquilla', '3144444444', 'sofia.marin@example.com'),
    ('Juan',   'Perez',    'Mexico',   'Guadalajara',  '3155555555', 'juan.perez@example.com'),
    ('Marta',  'Lopez',    'Spain',    'Valencia',  '3166666666', 'marta.lopez@example.com'),
    ('Pedro',  'Silva',    'Brazil',   'Sao Paulo', '3177777777', 'pedro.silva@example.com'),
    ('Ana',    'Torres',   'Chile',    'Santiago',  '3188888888', 'ana.torres@example.com'),
    ('Luis',   'Ramirez',  'Peru',     'Lima',      '3199999999', 'luis.ramirez@example.com');

INSERT INTO api.resources ("name", trademark, stock, price, description, tags, active) VALUES
    ('Steel Pizza Cutter', 'Jast Inc',    30, 12.50, 'Rotary cutter',      'kitchen,steel', true),
    ('Granite Chair',      'Kuhn Group',  15,  89.99, 'Outdoor seating',   'garden',        true),
    ('Rubber Keyboard',    'Bahringer',   42,  24.00, 'Splash proof',      'office,input',  true),
    ('Cotton Towel',       'Hilll LLC',    8,   6.75, 'Bath size',         'home',          true),
    ('Wooden Desk',        'Rau Sons',     3, 249.00, 'Solid oak',         'office',        true),
    ('Plastic Bottle',     'Torp Inc',   120,   2.20, 'Reusable, 750ml',   'sport',         true),
    ('Frozen Salad',       'Reilly LLC',  22,   4.10, 'Ready to eat',      'food',          false),
    ('Metal Lamp',         'Ortiz Group', 17,  35.40, 'Desk lamp',         'office,light',  true),
    ('Soft Gloves',        'Hane Inc',    64,   9.95, 'Winter gloves',     'apparel',       true),
    ('Fresh Soap',         'Koch LLC',    51,   3.30, 'Unscented',         'home',          true),
    ('Handmade Mouse',     'Lynch Group', 12,  19.99, 'Ergonomic',         'office,input',  true),
    ('Sleek Shoes',        'Ward Sons',   26,  74.50, 'Running',           'apparel,sport', true),
    ('Small Ball',         'Beier Inc',   90,   5.60, 'Training ball',     'sport',         true),
    ('Tasty Cheese',       'Funk LLC',    14,   8.20, 'Aged 12 months',    'food',          true),
    ('Licensed Hat',       'Blick Group', 33,  16.00, 'Adjustable',        'apparel',       true);

-- --------------------------------------------------------- PostgREST roles
--
-- PostgREST authenticates as `authenticator` and switches to the anonymous role for
-- unauthenticated requests. Auth is out of scope for this suite (see the README's
-- non-goals), so web_anon gets full DML on the two tables and nothing else.

CREATE ROLE web_anon NOLOGIN;
GRANT USAGE ON SCHEMA api TO web_anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON api.clients, api.resources TO web_anon;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA api TO web_anon;

CREATE ROLE authenticator NOINHERIT LOGIN PASSWORD 'postgrest_pw';
GRANT web_anon TO authenticator;
