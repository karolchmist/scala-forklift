package scala.migrations

trait Migration[T]{
  def id : T
  def up : Unit
}

trait MigrationManager[T]{
  var migrations : Seq[Migration[T]] = List()
  def ids = migrations.map(_.id)
  def alreadyAppliedIds : Seq[T]
  def notYetAppliedMigrations = migrations.filter(
	m => !alreadyAppliedIds.exists(_ == m.id))

  def init: Unit
  def latest: T
  def beforeApply(migration:Migration[T]){}
  def afterApply(migration:Migration[T])
  def up {
    val ids = migrations.map(_.id)
    assert( ids.toSet == Range(1,ids.size+1).toSet )
    while(notYetAppliedMigrations.size > 0){
      singleUp
    }
  }

  def singleUp {
    if(notYetAppliedMigrations.size > 0){
      val migration = notYetAppliedMigrations.head
      try{
        beforeApply(migration)
        migration.up
        afterApply(migration)
      } catch {
        case e:Exception => rollback; throw e
      }
    }
  }

  def rollback: Unit
  def reset: Unit
}
