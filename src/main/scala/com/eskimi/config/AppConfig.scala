package com.eskimi.config

import org.slf4j.LoggerFactory
import pureconfig._
import pureconfig.generic.auto._

final case class AppConfig(api: ApiConfig)

object AppConfig {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def apply(resource: String = "application.conf"): AppConfig = {
    ConfigSource.resources(resource).load[AppConfig] match {
      case Left(errors) =>
        val msg = s"Unable to load service configuration (AppConfig)"
        logger.error(
          s"$msg \n${errors.toList.map(_.description).mkString("* ", "\n*", "")}"
        )
        throw new IllegalStateException(msg)

      case Right(config) =>
        logger.debug(s"Successfully loaded configuration (AppConfig), $config")
        config
    }
  }

}
