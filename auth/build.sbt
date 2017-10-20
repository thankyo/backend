import play.sbt.routes.RoutesKeys

RoutesKeys.routesImport += "com.clemble.loveit.auth.controller._"

coverageExcludedFiles := """.*\.template\.scala;.*JavaScriptReverseRoutes.*;.*ReverseRoutes.*;.*Routes.*;.*Module.*;"""
