package ru.androidlearning.jpgtopngconverter.scheduler

object WorkSchedulersFactory {
    fun create(): WorkSchedulers = WorkSchedulersImpl()
}
