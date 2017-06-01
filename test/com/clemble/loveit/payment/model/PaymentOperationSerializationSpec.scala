package com.clemble.loveit.payment.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.{Generator, PaymentOperationGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class PaymentOperationSerializationSpec extends SerializationSpec[PaymentOperation]
