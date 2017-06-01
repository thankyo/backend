package com.clemble.loveit.test

package object util {

  implicit val resourceGenerator = ResourceGenerator
  implicit val commonSocialProfileGenerator = CommonSocialProfileGenerator
  implicit val paymentTransactionGenerator = PaymentTransactionGenerator
  implicit val repositoryErrorGenerator = RepositoryErrorGenerator
  implicit val repositoryExceptionGenerator = RepositoryExceptionGenerator
  implicit val thankExceptionGenerator = ThankExceptionGenerator
  implicit val userExceptionGenerator = UserExceptionGenerator
  implicit val bankDetailsGenerator = BankDetailsGenerator
  implicit val paymentOperationGenerator = PaymentOperationGenerator
  implicit val thankTransactionGenerator = ThankTransactionGenerator
  implicit val verificationGenerator = ROVerificationGenerator
  implicit val thankGenerator = ThankGenerator
  implicit val userGenerator = UserGenerator

  def some[T](implicit gen: Generator[T]) = gen.generate()

}
