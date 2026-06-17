-- 创建数据库（如需要）
-- CREATE DATABASE fishing_db;

-- 渔船表
CREATE TABLE IF NOT EXISTS fishing_vessel (
    id BIGSERIAL PRIMARY KEY,
    vessel_name VARCHAR(100) NOT NULL,
    vessel_code VARCHAR(50) UNIQUE NOT NULL,
    captain VARCHAR(50),
    tonnage DECIMAL(10, 2),
    length DECIMAL(10, 2),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE fishing_vessel IS '远洋渔船信息表';
COMMENT ON COLUMN fishing_vessel.vessel_name IS '渔船名称';
COMMENT ON COLUMN fishing_vessel.vessel_code IS '渔船编号';
COMMENT ON COLUMN fishing_vessel.captain IS '船长姓名';
COMMENT ON COLUMN fishing_vessel.tonnage IS '吨位（吨）';
COMMENT ON COLUMN fishing_vessel.length IS '船长（米）';
COMMENT ON COLUMN fishing_vessel.status IS '状态：1-在航 2-停泊 3-维修';

-- GPS轨迹数据表
CREATE TABLE IF NOT EXISTS vessel_gps_track (
    id BIGSERIAL PRIMARY KEY,
    vessel_id BIGINT NOT NULL,
    longitude DECIMAL(12, 8) NOT NULL,
    latitude DECIMAL(12, 8) NOT NULL,
    speed DECIMAL(8, 2),
    heading DECIMAL(6, 2),
    track_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_vessel_gps_vessel_time ON vessel_gps_track(vessel_id, track_time);
CREATE INDEX IF NOT EXISTS idx_vessel_gps_time ON vessel_gps_track(track_time);

COMMENT ON TABLE vessel_gps_track IS '渔船GPS轨迹数据表';
COMMENT ON COLUMN vessel_gps_track.vessel_id IS '渔船ID';
COMMENT ON COLUMN vessel_gps_track.longitude IS '经度';
COMMENT ON COLUMN vessel_gps_track.latitude IS '纬度';
COMMENT ON COLUMN vessel_gps_track.speed IS '航速（节）';
COMMENT ON COLUMN vessel_gps_track.heading IS '航向（度）';
COMMENT ON COLUMN vessel_gps_track.track_time IS '定位时间';

-- 气象网格数据表
CREATE TABLE IF NOT EXISTS weather_grid (
    id BIGSERIAL PRIMARY KEY,
    grid_time TIMESTAMP NOT NULL,
    longitude DECIMAL(8, 4) NOT NULL,
    latitude DECIMAL(8, 4) NOT NULL,
    pressure DECIMAL(10, 2),
    wind_speed DECIMAL(8, 2),
    wind_direction DECIMAL(6, 2),
    wave_height DECIMAL(8, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_weather_grid_time ON weather_grid(grid_time);
CREATE INDEX IF NOT EXISTS idx_weather_grid_lon_lat_time ON weather_grid(longitude, latitude, grid_time);

COMMENT ON TABLE weather_grid IS '气象海况网格数据表';
COMMENT ON COLUMN weather_grid.grid_time IS '气象数据时间';
COMMENT ON COLUMN weather_grid.longitude IS '网格点经度';
COMMENT ON COLUMN weather_grid.latitude IS '网格点纬度';
COMMENT ON COLUMN weather_grid.pressure IS '气压（hPa）';
COMMENT ON COLUMN weather_grid.wind_speed IS '风速（m/s）';
COMMENT ON COLUMN weather_grid.wind_direction IS '风向（度）';
COMMENT ON COLUMN weather_grid.wave_height IS '浪高（米）';
