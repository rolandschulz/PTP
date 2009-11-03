REPHRASER ENGINE - TEXT EXAMPLE

This is a simple example of how to contribute a refactoring to the Rephraser Refactoring UI.
(Note that this does *not* use the VPG components of the Rephraser engine.)

(1) This plug-in contributes a "Convert Tabs to Spaces" item to the Refactor menu.  The Refactor
    menu will be visible in the menu bar in the Resource and Java perspectives (among others).
    To use the refactoring, open any text file (including source code), and select
    Refactor > Convert Tabs to Spaces.  A Refactor sub-menu should also be available when you
    select and then right-click one or more text files, folders, or projects in the Project
    Explorer view (i.e., when the current selection in the workbench is a resource).

(2) The Refactoring itself is defined in ConvertTabsToSpacesRefactoring.java.  That class is
    contributed to the Rephraser Engine's refactoring extension point.

(3) A "resource filter" is also contributed, which determines when this refactoring will be
    available.  We contribute a resource filter class defined in TextFileResourceFilter.java,
    which accepts any file that has a text content type (according to the Eclipse Platform's
    content type manager); this includes .txt files as well as most source code (e.g., .java files,
    .c files, etc.)

For details, see the plugin.xml file, and then look in the classes referenced in there.

Notice that, in the plugin.xml file, we contribute the ConvertTabsToSpacesRefactoring class, but
we don't give it a name, and we don't provide any user interface for it.  In this case,
* the name is determined by calling the refactoring's getName() method, and
* the user input dialog is constructed by looking for @UserInputString and @UserInputBoolean
  annotations in the ConvertTabsToSpacesRefactoring class.  In this case, there is one method with
  a @UserInputBoolean annotation, so the dialog has a single checkbox.  When the user checks or
  unchecks the checkbox, that method is invoked on the refactoring class.
Of course, it is possible to customize the name and/or the user interface, but we do not need to
in this simple example.

--Jeff Overbey 10/14/09