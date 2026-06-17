-- 清空现有数据
TRUNCATE TABLE vessel_gps_track, weather_grid, fishing_vessel RESTART IDENTITY;

-- 插入渔船测试数据
INSERT INTO fishing_vessel (vessel_name, vessel_code, captain, tonnage, length, status) VALUES
('远洋一号', 'YY-001', '张船长', 350.50, 38.50, 1),
('海洋之星', 'YY-002', '李船长', 520.00, 45.20, 1),
('东海蛟龙', 'YY-003', '王船长', 280.80, 32.00, 2),
('蓝鲸号', 'YY-004', '赵船长', 680.00, 52.00, 1),
('福远渔668', 'YY-005', '陈船长', 420.00, 40.00, 1);

-- 插入渔船1(远洋一号)的GPS轨迹 - 从舟山附近出发往东南方向航行
-- 时间范围: 过去7天, 每30分钟一个点
DO $$
DECLARE
    v_id BIGINT := 1;
    start_time TIMESTAMP := NOW() - INTERVAL '7 days';
    base_lon DECIMAL(12,8) := 122.20000000;
    base_lat DECIMAL(12,8) := 30.00000000;
    i INTEGER;
    t TIMESTAMP;
    curr_lon DECIMAL(12,8);
    curr_lat DECIMAL(12,8);
    speed DECIMAL(8,2);
    heading DECIMAL(6,2);
BEGIN
    FOR i IN 0..336 LOOP
        t := start_time + (i || ' minutes')::INTERVAL * 30;
        curr_lon := base_lon + (i * 0.015) + (RANDOM() * 0.02 - 0.01);
        curr_lat := base_lat - (i * 0.008) + (RANDOM() * 0.02 - 0.01) + SIN(i * 0.1) * 0.3;
        speed := 8.0 + RANDOM() * 6.0;
        heading := 120.0 + (RANDOM() * 40 - 20);
        
        INSERT INTO vessel_gps_track (vessel_id, longitude, latitude, speed, heading, track_time)
        VALUES (v_id, curr_lon, curr_lat, speed, heading, t);
    END LOOP;
END $$;

-- 插入渔船2(海洋之星)的GPS轨迹 - 从上海出发往东航行
DO $$
DECLARE
    v_id BIGINT := 2;
    start_time TIMESTAMP := NOW() - INTERVAL '5 days';
    base_lon DECIMAL(12,8) := 121.50000000;
    base_lat DECIMAL(12,8) := 31.20000000;
    i INTEGER;
    t TIMESTAMP;
    curr_lon DECIMAL(12,8);
    curr_lat DECIMAL(12,8);
    speed DECIMAL(8,2);
    heading DECIMAL(6,2);
BEGIN
    FOR i IN 0..240 LOOP
        t := start_time + (i || ' minutes')::INTERVAL * 30;
        curr_lon := base_lon + (i * 0.020) + COS(i * 0.05) * 0.2;
        curr_lat := base_lat + SIN(i * 0.08) * 0.4 + (RANDOM() * 0.02 - 0.01);
        speed := 10.0 + RANDOM() * 5.0;
        heading := 85.0 + (RANDOM() * 30 - 15);
        
        INSERT INTO vessel_gps_track (vessel_id, longitude, latitude, speed, heading, track_time)
        VALUES (v_id, curr_lon, curr_lat, speed, heading, t);
    END LOOP;
END $$;

-- 插入渔船4(蓝鲸号)的GPS轨迹 - 从厦门出发往南航行
DO $$
DECLARE
    v_id BIGINT := 4;
    start_time TIMESTAMP := NOW() - INTERVAL '6 days';
    base_lon DECIMAL(12,8) := 118.10000000;
    base_lat DECIMAL(12,8) := 24.50000000;
    i INTEGER;
    t TIMESTAMP;
    curr_lon DECIMAL(12,8);
    curr_lat DECIMAL(12,8);
    speed DECIMAL(8,2);
    heading DECIMAL(6,2);
BEGIN
    FOR i IN 0..288 LOOP
        t := start_time + (i || ' minutes')::INTERVAL * 30;
        curr_lon := base_lon + SIN(i * 0.06) * 0.5 + (RANDOM() * 0.02 - 0.01);
        curr_lat := base_lat - (i * 0.012) + (RANDOM() * 0.02 - 0.01);
        speed := 9.0 + RANDOM() * 5.0;
        heading := 180.0 + (RANDOM() * 30 - 15);
        
        INSERT INTO vessel_gps_track (vessel_id, longitude, latitude, speed, heading, track_time)
        VALUES (v_id, curr_lon, curr_lat, speed, heading, t);
    END LOOP;
END $$;

-- 插入气象网格数据 (覆盖东经115-130度, 北纬20-40度区域, 每1度一个网格点, 每3小时一次数据)
DO $$
DECLARE
    t TIMESTAMP;
    lon_val DECIMAL(8,4);
    lat_val DECIMAL(8,4);
    pressure DECIMAL(10,2);
    wind_speed DECIMAL(8,2);
    wind_dir DECIMAL(6,2);
    wave_height DECIMAL(8,2);
    time_idx INTEGER;
    lon_idx INTEGER;
    lat_idx INTEGER;
    center_lon DECIMAL := 122.0;
    center_lat DECIMAL := 30.0;
    dist DECIMAL;
    time_factor DECIMAL;
BEGIN
    FOR time_idx IN 0..56 LOOP
        t := NOW() - INTERVAL '7 days' + (time_idx || ' hours')::INTERVAL * 3;
        time_factor := SIN(time_idx * 0.3) * 5;
        
        FOR lon_idx IN 0..15 LOOP
            FOR lat_idx IN 0..20 LOOP
                lon_val := 115.0 + lon_idx;
                lat_val := 20.0 + lat_idx;
                
                dist := SQRT(POWER(lon_val - center_lon, 2) + POWER(lat_val - center_lat, 2));
                
                pressure := 1013.0 + time_factor + SIN(lon_idx * 0.5) * 3 + COS(lat_idx * 0.4) * 2 - dist * 0.3;
                wind_speed := GREATEST(0.5, 8.0 + time_factor + SIN(lon_idx * 0.3 + lat_idx * 0.2) * 5 + RANDOM() * 2);
                wind_dir := (90 + time_factor * 10 + SIN(lon_idx * 0.2) * 30 + RANDOM() * 20)::DECIMAL;
                wave_height := GREATEST(0.1, wind_speed * 0.25 + RANDOM() * 0.5);
                
                INSERT INTO weather_grid (grid_time, longitude, latitude, pressure, wind_speed, wind_direction, wave_height)
                VALUES (t, lon_val, lat_val, pressure, wind_speed, wind_dir, wave_height);
            END LOOP;
        END LOOP;
    END LOOP;
END $$;
