package com.eskimi.repository

trait Repository[K, T] {

  // Classic CRUD operations
  def create(entity: T): T
  def read(key: K): Option[T]
  def update(entity: T): T
  def delete(key: K): Option[T]

  def findAll(): Seq[T]

}
