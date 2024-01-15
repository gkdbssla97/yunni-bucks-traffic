CREATE TABLE IF NOT EXISTS `coupon`
(
    `id`              bigint NOT NULL,
    `coupon_use`      varchar(255) DEFAULT NULL,
    `create_at`       datetime     DEFAULT NULL,
    `discount_rate`   double       DEFAULT NULL,
    `expire_at`       datetime     DEFAULT NULL,
    `identity_number` varchar(255) DEFAULT NULL,
    `coupon_name`     varchar(255) DEFAULT NULL,
    `version`         bigint       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `hibernate_sequence`
(
    `next_val` bigint DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO hibernate_sequence VALUES (1);

CREATE TABLE IF NOT EXISTS `member`
(
    `id`          bigint NOT NULL,
    `create_at`   datetime       DEFAULT NULL,
    `update_at`   datetime       DEFAULT NULL,
    `city`        varchar(255)   DEFAULT NULL,
    `detail`      varchar(255)   DEFAULT NULL,
    `district`    varchar(255)   DEFAULT NULL,
    `zip_code`    varchar(255)   DEFAULT NULL,
    `email`       varchar(255)   DEFAULT NULL,
    `total_price` decimal(19, 2) DEFAULT NULL,
    `user_name`   varchar(255)   DEFAULT NULL,
    `order_count` int            DEFAULT NULL,
    `password`    varchar(255)   DEFAULT NULL,
    `user_rank`   varchar(255)   DEFAULT NULL,
    `version`     bigint         DEFAULT NULL,
    `coupon_id`   bigint         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKl6061bdtr8ca300qyhlhkqapg` (`coupon_id`),
    CONSTRAINT `FKl6061bdtr8ca300qyhlhkqapg` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `card`
(
    `card_id`       bigint NOT NULL,
    `create_at`     datetime     DEFAULT NULL,
    `update_at`     datetime     DEFAULT NULL,
    `card_password` varchar(4)   DEFAULT NULL,
    `number`        varchar(20)  DEFAULT NULL,
    `valid_thru`    varchar(255) DEFAULT NULL,
    `user_id`       bigint       DEFAULT NULL,
    PRIMARY KEY (`card_id`),
    KEY `FKkngharefss6rqb6r7io1ou5u6` (`user_id`),
    CONSTRAINT `FKkngharefss6rqb6r7io1ou5u6` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `cart`
(
    `id`        bigint NOT NULL,
    `version`   bigint DEFAULT NULL,
    `member_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKix170nytunweovf2v9137mx2o` (`member_id`),
    CONSTRAINT `FKix170nytunweovf2v9137mx2o` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `menu`
(
    `dtype`         varchar(31) NOT NULL,
    `id`            bigint      NOT NULL,
    `create_at`     datetime       DEFAULT NULL,
    `description`   varchar(255)   DEFAULT NULL,
    `menu_size`     varchar(255)   DEFAULT NULL,
    `menu_status`   varchar(255)   DEFAULT NULL,
    `menu_type`     varchar(255)   DEFAULT NULL,
    `carbohydrates` int         NOT NULL,
    `fats`          int         NOT NULL,
    `kcal`          int         NOT NULL,
    `proteins`      int         NOT NULL,
    `order_count`   int         NOT NULL,
    `total_price`   decimal(19, 2) DEFAULT NULL,
    `score`         double      NOT NULL,
    `stock`         int            DEFAULT NULL,
    `title`         varchar(255)   DEFAULT NULL,
    `update_at`     datetime       DEFAULT NULL,
    `version`       bigint         DEFAULT NULL,
    `view_count`    int         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `cart_item`
(
    `id`      bigint NOT NULL,
    `cart_id` bigint DEFAULT NULL,
    `menu_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK1uobyhgl1wvgt1jpccia8xxs3` (`cart_id`),
    KEY `FKpcv0bm5di40xdnacpjrxn39bl` (`menu_id`),
    CONSTRAINT `FK1uobyhgl1wvgt1jpccia8xxs3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`),
    CONSTRAINT `FKpcv0bm5di40xdnacpjrxn39bl` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `menu_review`
(
    `id`        bigint NOT NULL,
    `comments`  text,
    `create_at` datetime DEFAULT NULL,
    `update_at` datetime DEFAULT NULL,
    `member_id` bigint   DEFAULT NULL,
    `menu_id`   bigint   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKfspv0dld0banwpccuiy8nelt8` (`member_id`),
    KEY `FKbq51s0styx4qrm18o70ghcwnj` (`menu_id`),
    FULLTEXT KEY `ftx_comments` (`comments`) /*!50100 WITH PARSER `ngram` */,
    CONSTRAINT `FKbq51s0styx4qrm18o70ghcwnj` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`),
    CONSTRAINT `FKfspv0dld0banwpccuiy8nelt8` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `menu_thumbnail`
(
    `id`               bigint NOT NULL,
    `create_at`        datetime     DEFAULT NULL,
    `origin_file_name` varchar(255) DEFAULT NULL,
    `stored_file_name` varchar(255) DEFAULT NULL,
    `update_at`        datetime     DEFAULT NULL,
    `menu_id`          bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK8sxyy7tqt6xlbu03254bq563` (`menu_id`),
    CONSTRAINT `FK8sxyy7tqt6xlbu03254bq563` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `orders`
(
    `id`          bigint NOT NULL,
    `create_at`   datetime       DEFAULT NULL,
    `order_name`  varchar(255)   DEFAULT NULL,
    `total_price` decimal(19, 2) DEFAULT NULL,
    `pay_status`  varchar(255)   DEFAULT NULL,
    `status`      varchar(255)   DEFAULT NULL,
    `update_at`   datetime       DEFAULT NULL,
    `cart_id`     bigint         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKtpihbdn6ws0hu56camb0bg2to` (`cart_id`),
    CONSTRAINT `FKtpihbdn6ws0hu56camb0bg2to` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `card_payment`
(
    `card_payment_id`       bigint NOT NULL,
    `approved_at`           datetime       DEFAULT NULL,
    `requested_at`          datetime       DEFAULT NULL,
    `cancel_payment_at`     datetime       DEFAULT NULL,
    `cancel_reason`         varchar(255)   DEFAULT NULL,
    `card_expiration_month` varchar(255)   DEFAULT NULL,
    `card_expiration_year`  varchar(255)   DEFAULT NULL,
    `card_number`           varchar(255)   DEFAULT NULL,
    `card_password`         varchar(255)   DEFAULT NULL,
    `customer_name`         varchar(255)   DEFAULT NULL,
    `order_uuid`            varchar(255)   DEFAULT NULL,
    `payment_key`           varchar(255)   DEFAULT NULL,
    `payment_status`        varchar(255)   DEFAULT NULL,
    `payment_type`          varchar(255)   DEFAULT NULL,
    `refund_amount`         decimal(19, 2) DEFAULT NULL,
    `order_id`              bigint         DEFAULT NULL,
    PRIMARY KEY (`card_payment_id`),
    KEY `FKe4ngxj0rreb05pnaolcf0b0xd` (`order_id`),
    CONSTRAINT `FKe4ngxj0rreb05pnaolcf0b0xd` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `delivery`
(
    `dtype`      varchar(31) NOT NULL,
    `id`         bigint      NOT NULL,
    `city`       varchar(255) DEFAULT NULL,
    `detail`     varchar(255) DEFAULT NULL,
    `district`   varchar(255) DEFAULT NULL,
    `zip_code`   varchar(255) DEFAULT NULL,
    `create_at`  datetime     DEFAULT NULL,
    `status`     varchar(255) DEFAULT NULL,
    `type`       varchar(255) DEFAULT NULL,
    `update_at`  datetime     DEFAULT NULL,
    `reserve_at` datetime     DEFAULT NULL,
    `order_id`   bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKu4e8rjwmg09vmas3ccjwglso` (`order_id`),
    CONSTRAINT `FKu4e8rjwmg09vmas3ccjwglso` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `payment_details`
(
    `id`              bigint NOT NULL,
    `approved_at`     datetime     DEFAULT NULL,
    `requested_at`    datetime     DEFAULT NULL,
    `balance_amount`  int    NOT NULL,
    `payment_type`    varchar(255) DEFAULT NULL,
    `supplied_amount` int    NOT NULL,
    `total_amount`    int    NOT NULL,
    `vat`             int    NOT NULL,
    `order_id`        bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK34yjcjptgtt05syk6x0t8s35b` (`order_id`),
    CONSTRAINT `FK34yjcjptgtt05syk6x0t8s35b` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;


