task("print") {
    doLast {
        val list: ListProperty<String> = project.objects.listProperty()
        list.empty()

        // Resolve the list
        logger.quiet("The list contains: " + list.get())

        // Add elements to the empty list
        list.add(project.provider { "element-1" })  // Add a provider element
        list.add("element-2")                       // Add a concrete element

        // Resolve the list
        logger.quiet("The list contains: " + list.get())

        // Overwrite the entire list with a new list
        list.set(listOf("element-3", "element-4"))

        // Resolve the list
        logger.quiet("The list contains: " + list.get())

        // Add more elements through a list provider
        list.addAll(project.provider { listOf("element-5", "element-6") })

        // Resolve the list
        logger.quiet("The list contains: " + list.get())
    }
}
