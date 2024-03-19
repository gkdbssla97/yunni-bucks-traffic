SET search_path TO public;
CREATE SCHEMA IF NOT EXISTS public;
create table orders
(
    id         bigint not null
        primary key,
    create_at  timestamp,
    order_name varchar(255),
    totalprice numeric(19, 2),
    paystatus  varchar(255),
    status     varchar(255),
    update_at  timestamp,
    cart_id    bigint
);

alter table orders
    owner to mydb_user;

create table coupon
(
    id              bigint not null
        primary key,
    couponuse       varchar(255),
    create_at       timestamp,
    discount_rate   double precision,
    expire_at       timestamp,
    identity_number varchar(255),
    coupon_name     varchar(255),
    version         bigint
);

alter table coupon
    owner to mydb_user;

create table member
(
    id          bigint not null
        primary key,
    createat    timestamp,
    updateat    timestamp,
    city        varchar(255),
    detail      varchar(255),
    district    varchar(255),
    zipcode     varchar(255),
    email       varchar(255),
    totalprice  numeric(19, 2),
    user_name   varchar(255),
    order_count integer,
    password    varchar(255),
    userrank    varchar(255),
    version     bigint,
    coupon_id   bigint
        constraint fk7qp2y9cw3fid6nl0fdcjkvdel
            references coupon
);

alter table member
    owner to mydb_user;

create table card
(
    card_id      bigint not null
        primary key,
    createat     timestamp,
    updateat     timestamp,
    cardpassword varchar(4),
    number       varchar(20),
    validthru    varchar(255),
    user_id      bigint
        constraint fk8fplowntyfnqso4kl47axrjsl
            references member
);

alter table card
    owner to mydb_user;

create table cart
(
    id        bigint not null
        primary key,
    version   bigint,
    member_id bigint
        constraint fk62b80wq5in3dmemy7ac86n6b1
            references member
);

alter table cart
    owner to mydb_user;

create table menu
(
    dtype         varchar(31)      not null,
    id            bigint           not null
        primary key,
    create_at     timestamp,
    description   varchar(255),
    menusize      varchar(255),
    menustatus    varchar(255),
    menutype      varchar(255),
    carbohydrates integer          not null,
    fats          integer          not null,
    kcal          integer          not null,
    proteins      integer          not null,
    ordercount    integer          not null,
    totalprice    numeric(19, 2),
    score         double precision not null,
    stock         integer,
    title         varchar(255),
    update_at     timestamp,
    version       bigint,
    viewcount     integer          not null
);

alter table menu
    owner to mydb_user;

create table cartitem
(
    id      bigint not null
        primary key,
    cart_id bigint
        constraint fkes77rm51hdhw46u6wdlblndq2
            references cart,
    menu_id bigint
        constraint fkibh9oh62bj8c9wfhxcf075oq7
            references menu
);

alter table cartitem
    owner to mydb_user;

create table menu_review
(
    id        bigint not null
        primary key,
    comments  text,
    create_at timestamp,
    update_at timestamp,
    member_id bigint
        constraint fkjv5b9g3bnx8b1jgdho7yyr73e
            references member,
    menu_id   bigint
        constraint fk1xu83uc0dfxjy7txwjq10a8wj
            references menu
);

alter table menu_review
    owner to mydb_user;

create index comments_idx
    on menu_review using gin (to_tsvector('english'::regconfig, comments));

create table menuthumbnail
(
    id             bigint not null
        primary key,
    createat       timestamp,
    originfilename varchar(255),
    storedfilename varchar(255),
    updateat       timestamp,
    menu_id        bigint
        constraint fkbwbi2t8oij1aytkrv6pyhfgxe
            references menu
);

alter table menuthumbnail
    owner to mydb_user;

