package com.clemble.loveit.payment.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.{Generator, PaymentTransactionGenerator}
import play.api.libs.json.Format

class PaymentTransactionSerializationSpec extends SerializationSpec[PaymentTransaction]
