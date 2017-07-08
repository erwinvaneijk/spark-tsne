/*
 * Copyright (c) 2017, Forest Fang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import Keys._
import com.typesafe.sbt.GitPlugin.autoImport._
import scala.language.experimental.macros
import scala.reflect.macros.Context

object Common {
  lazy val commonSettings = Seq(
    organization in ThisBuild := "com.github.saurfang",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation", "-feature"),
    //git.useGitDescribe := true,
    git.baseVersion := "0.0.1",
    parallelExecution in test := false,
    // Skip doing tests in assembly.
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

  def tsneProject(path: String): Project = macro tsneProjectMacroImpl

  def tsneProjectMacroImpl(c: Context)(path: c.Expr[String]) = {
    import c.universe._
    reify {
      (Project.projectMacroImpl(c).splice in file(path.splice)).
        settings(name := path.splice).
        settings(Dependencies.Versions).
        settings(commonSettings: _*)
    }
  }
}