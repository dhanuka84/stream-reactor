package com.datamountaineer.streamreactor.connect.influx.config

import com.datamountaineer.connector.config.Config
import com.datamountaineer.streamreactor.connect.errors.ThrowErrorPolicy
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.common.config.types.Password
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class InfluxSettingsTest extends WordSpec with Matchers with MockitoSugar {

  val MEASURE_NAME = "someMeasurement"
  val TOPIC_NAME = "mykafkatopic"
  val QUERY_ALL = s"INSERT INTO $MEASURE_NAME SELECT * FROM $TOPIC_NAME"
  val QUERY_SELECT = s"INSERT INTO $MEASURE_NAME SELECT lastName as surname, firstName FROM $TOPIC_NAME"
  val QUERY_SELECT_AND_TIMESTAMP = s"INSERT INTO $MEASURE_NAME SELECT * FROM $TOPIC_NAME WITHTIMESTAMP ts"
  val QUERY_SELECT_AND_TIMESTAMP_SYSTEM = s"INSERT INTO $MEASURE_NAME SELECT * FROM $TOPIC_NAME WITHTIMESTAMP ${Config.TIMESTAMP}"

  "raise a configuration exception if the connection url is missing" in {
    intercept[ConfigException] {
      val config = mock[InfluxSinkConfig]
      when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn("mydb")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn("myuser")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("apass")
      when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
      when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_ALL)
      InfluxSettings(config)
    }
  }

  "raise a configuration exception if the database is not set" in {
    intercept[ConfigException] {
      val config = mock[InfluxSinkConfig]
      when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn("http://localhost:8081")
      when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn("")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn("myuser")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("apass")
      when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
      when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_ALL)
      InfluxSettings(config)
    }
  }

  "raise a configuration exception if the user is not set" in {
    intercept[ConfigException] {
      val config = mock[InfluxSinkConfig]
      when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn("http://localhost:8081")
      when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn("mydatbase")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn("")
      when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("apass")
      when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
      when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_ALL)
      InfluxSettings(config)
    }
  }

  "create a settings with all fields" in {
    val url = "http://localhost:8081"
    val database = "mydatabase"
    val user = "myuser"
    val config = mock[InfluxSinkConfig]
    when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn(url)
    when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn(database)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn(user)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn(null)
    when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
    when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_ALL)
    val settings = InfluxSettings(config)
    settings.connectionUrl shouldBe url
    settings.database shouldBe database
    settings.user shouldBe user
    settings.password shouldBe null
    settings.errorPolicy shouldBe ThrowErrorPolicy()
    settings.topicToMeasurementMap shouldBe Map(TOPIC_NAME -> MEASURE_NAME)
    settings.fieldsExtractorMap.size shouldBe 1
    settings.fieldsExtractorMap(TOPIC_NAME).includeAllFields shouldBe true
    settings.fieldsExtractorMap(TOPIC_NAME).fieldsAliasMap shouldBe Map.empty
    settings.fieldsExtractorMap(TOPIC_NAME).timestampField shouldBe None
  }

  "create a settings with selected fields" in {
    val url = "http://localhost:8081"
    val database = "mydatabase"
    val user = "myuser"
    val config = mock[InfluxSinkConfig]
    when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn(url)
    when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn(database)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn(user)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("mememe")
    when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
    when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_SELECT)
    val settings = InfluxSettings(config)
    settings.connectionUrl shouldBe url
    settings.database shouldBe database
    settings.user shouldBe user
    settings.password shouldBe "mememe"
    settings.errorPolicy shouldBe ThrowErrorPolicy()
    settings.topicToMeasurementMap shouldBe Map(TOPIC_NAME -> MEASURE_NAME)
    settings.fieldsExtractorMap.size shouldBe 1
    settings.fieldsExtractorMap(TOPIC_NAME).includeAllFields shouldBe false
    settings.fieldsExtractorMap(TOPIC_NAME).fieldsAliasMap shouldBe Map("firstName" -> "firstName", "lastName" -> "surname")
    settings.fieldsExtractorMap(TOPIC_NAME).timestampField shouldBe None
  }

  "create a settings with selected fields with timestamp set to a field" in {
    val url = "http://localhost:8081"
    val database = "mydatabase"
    val user = "myuser"
    val config = mock[InfluxSinkConfig]
    when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn(url)
    when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn(database)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn(user)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("mememe")
    when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
    when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_SELECT_AND_TIMESTAMP)
    val settings = InfluxSettings(config)
    settings.connectionUrl shouldBe url
    settings.database shouldBe database
    settings.user shouldBe user
    settings.password shouldBe "mememe"
    settings.errorPolicy shouldBe ThrowErrorPolicy()
    settings.topicToMeasurementMap shouldBe Map(TOPIC_NAME -> MEASURE_NAME)
    settings.fieldsExtractorMap.size shouldBe 1
    settings.fieldsExtractorMap(TOPIC_NAME).includeAllFields shouldBe true
    settings.fieldsExtractorMap(TOPIC_NAME).fieldsAliasMap shouldBe Map.empty
    settings.fieldsExtractorMap(TOPIC_NAME).timestampField shouldBe Some("ts")
  }

  "create a settings with selected fields with timestamp set to a sys_timestamp" in {
    val url = "http://localhost:8081"
    val database = "mydatabase"
    val user = "myuser"
    val config = mock[InfluxSinkConfig]
    when(config.getString(InfluxSinkConfig.INFLUX_URL_CONFIG)).thenReturn(url)
    when(config.getString(InfluxSinkConfig.INFLUX_DATABASE_CONFIG)).thenReturn(database)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_USER_CONFIG)).thenReturn(user)
    when(config.getString(InfluxSinkConfig.INFLUX_CONNECTION_PASSWORD_CONFIG)).thenReturn("mememe")
    when(config.getString(InfluxSinkConfig.ERROR_POLICY_CONFIG)).thenReturn("THROW")
    when(config.getString(InfluxSinkConfig.EXPORT_ROUTE_QUERY_CONFIG)).thenReturn(QUERY_SELECT_AND_TIMESTAMP_SYSTEM)
    val settings = InfluxSettings(config)
    settings.connectionUrl shouldBe url
    settings.database shouldBe database
    settings.user shouldBe user
    settings.password shouldBe "mememe"
    settings.errorPolicy shouldBe ThrowErrorPolicy()
    settings.topicToMeasurementMap shouldBe Map(TOPIC_NAME -> MEASURE_NAME)
    settings.fieldsExtractorMap.size shouldBe 1
    settings.fieldsExtractorMap(TOPIC_NAME).includeAllFields shouldBe true
    settings.fieldsExtractorMap(TOPIC_NAME).fieldsAliasMap shouldBe Map.empty
    settings.fieldsExtractorMap(TOPIC_NAME).timestampField shouldBe None
  }
}
