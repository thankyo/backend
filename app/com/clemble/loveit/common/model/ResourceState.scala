package com.clemble.loveit.common.model

sealed trait ResourceState
case object Loved extends ResourceState
case object NotLoved extends ResourceState
case object OwnerMissing extends ResourceState
