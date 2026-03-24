CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL UNIQUE,
  role_name VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  menu_name VARCHAR(64) NOT NULL,
  path VARCHAR(128) NOT NULL,
  component VARCHAR(128) NOT NULL,
  permission_code VARCHAR(128) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  visible TINYINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_menu (role_id, menu_id)
);

INSERT INTO sys_user (id, username, password, display_name, status)
VALUES (1, 'admin', '123456', 'System Admin', 1),
       (2, 'employee', '123456', 'Normal Employee', 1)
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);

INSERT INTO sys_role (id, role_code, role_name)
VALUES (1, 'ADMIN', 'Administrator'),
       (2, 'EMPLOYEE', 'Employee')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_menu (id, parent_id, menu_name, path, component, permission_code, sort_no, visible)
VALUES (1, 0, 'Dashboard', '/', 'HomeView', NULL, 1, 1),
       (2, 0, 'Approval Center', '/approval', 'Placeholder', 'approval:read', 2, 1),
       (3, 0, 'System Settings', '/system', 'Placeholder', 'system:manage', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, 1), (1, 2), (1, 3),
       (2, 1), (2, 2)
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);