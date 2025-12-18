/*
 Navicat Premium Data Transfer

 Source Server         : 蒋鹏丽本地
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : deepprofilematrix

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 31/10/2025 17:08:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_data
-- ----------------------------
DROP TABLE IF EXISTS `chat_data`;
CREATE TABLE `chat_data`  (
  `id` bigint NOT NULL COMMENT '会话记录ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `type` int NULL DEFAULT NULL COMMENT '会话类型',
  `chat_id` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '唯一会话ID',
  `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话标题',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '会话创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '会话编辑时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '会话状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_chat_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_data
-- ----------------------------

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `chat_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息类型',
  `timestamp` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '时间戳',
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(1:正常,0:删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_chat_id`(`chat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for jlpdf
-- ----------------------------
DROP TABLE IF EXISTS `jlpdf`;
CREATE TABLE `jlpdf`  (
  `id` bigint NOT NULL COMMENT '简历主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `pdf_score` double NULL DEFAULT NULL COMMENT '简历评分',
  `pdf_number` bigint NULL DEFAULT NULL COMMENT '简历排名',
  `pdf_update_count` int NULL DEFAULT NULL COMMENT '简历更新次数',
  `pdf_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '简历文件路径',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '简历创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '简历更新时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '简历状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_pdf_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户简历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jlpdf
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(225) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户密码',
  `avatar_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户头像',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '用户注册时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '用户更新时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '用户状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表，记录注册用户的基本信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'admin', 'https://cdn.jsdelivr.net/gh/Jane-Ai/Jane-Ai@main/static/img/avatar/1.png', '2025-07-12 00:00:00', '2023-07-12 00:00:00', 1);

SET FOREIGN_KEY_CHECKS = 1;
