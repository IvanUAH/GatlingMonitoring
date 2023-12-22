## Install and Run

Start docker-compose

Set path to Gatling report path in performance-management volumes:

    performance-management:
      container_name: performance-management
      image: ivanogn/performance-management:0.2.1
      ports:
        - "8081:80"
      volumes:
        - /PathToGatlingReportDir:/var/www/gatlingReport

Run:

    sudo docker-compose up -d

In docker-compose, you have configured:

1. Postgre DB: data stored in the location where docker-compose started
2. Gatling monitoring UI: performance-management
3. Influxdb: store real data from Gatling
4. Grafana: to see real-time performance data

Download or build a Gatling monitor and start:

    nohup java -jar GatlingMonitoring-1.0-SNAPSHOT.jar >/dev/null 2>&1 &
 ()
Then open http://localhost:8081. User: admin, password: admin123.

## Configuration

Open Settings page. 
 - Root path: path to your Gatling solution where the Maven POM file exists.
 - Path to report: path to Gatling results folder.
 - Host for live report: host where docker-compose is running + grafana port (http://hostNameOrIpAddress:3000)
 - In the Users tab, you can add or update Users.

## Gatling configuration

All configurations transfer to the Gatling test as environment variables.

Load model - Environment: configure the environment variables like hosts.
Load model - Scenarios: performance scenario and percentage usage of Users
Load model - Load model: loads profile configs like the number of users, duration, rump Up, and others.

## Run configuration

In Run configuration, you create a performance scenario by composition of Environment, Scenario, and Load model. Also from this page, you can run a performance test.

## Reports

List of reports. Run Id needs to separate each run. Original report - Gatling report, Live report - Gatling real-time report. Also, you can set the etalon report it will be used to compare reports with the same run configuration by pressing 'Compare reports'. By comparing two reports set 'Report to compare with' for the first report and press 'Compare reports' in the second.

## Dashboard and Analytics

In analytics, we have 3 group levels. When you set useGroupDurationMetric = true in gatling.conf you can see the dynamic of changes results.
In the dashboard, you can see how results changed for the last run vs the average last 20 runs.

