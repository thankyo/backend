package com.clemble.loveit.common.model

import java.time.LocalDateTime

case class ThankEvent(
                       user: UserID,
                       project: ProjectPointer,
                       url: Resource,
                       created: LocalDateTime = LocalDateTime.now()
                     ) extends ResourceAware with CreatedAware with UserAware with ProjectAware
