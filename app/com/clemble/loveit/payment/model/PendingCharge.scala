package com.clemble.loveit.payment.model

import java.time.LocalDate

import com.clemble.loveit.common.model.Project

case class PendingCharge (
                           project: Project,
                           transactions: List[PendingTransaction],
                           created: LocalDate
                        )
