package com.dekogen.gdeploy.apimarkup

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RestApi(val value: String, val version: String)
