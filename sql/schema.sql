CREATE TABLE oauth_user (
	id int NOT NULL IDENTITY(1,1) PRIMARY KEY,
	username nvarchar(254) NOT NULL,
	password nvarchar(254),
	is_active tinyint,
	role nvarchar(254),
	created_at datetime2(7),
	updated_at datetime2(7)
);

CREATE TABLE oauth_client_details (
  client_id VARCHAR(256) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256) NOT NULL,
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4000),
  autoapprove VARCHAR(256)
);

CREATE TABLE oauth_client_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256) PRIMARY KEY,
  user_name VARCHAR(256),
  client_id VARCHAR(256)
);

CREATE TABLE oauth_access_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication BLOB,
  refresh_token VARCHAR(256)
);

CREATE TABLE oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
);

CREATE TABLE oauth_code (
  code VARCHAR(256), authentication BLOB
);