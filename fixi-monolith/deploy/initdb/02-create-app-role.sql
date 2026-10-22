\getenv app_password FIXI_DATABASE_PASSWORD
CREATE ROLE fixi_app LOGIN PASSWORD :'app_password';
ALTER DATABASE fixi OWNER TO fixi_app;
GRANT USAGE, CREATE ON SCHEMA public TO fixi_app;
