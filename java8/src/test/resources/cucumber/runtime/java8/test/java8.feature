Feature: Java8

  Scenario: use the API with Java8 style
    Given I have 42 cukes in my belly
    Then I really have 42 cukes in my belly

  Scenario: another scenario which should have isolated state
    Given I have 42 cukes in my belly
    And something that isn't defined

  Scenario: Parameterless lambdas
    Given A statement with a simple match

  Scenario: Multi-param lambdas
    Given I will give you 1 and 2.2 and three and 4

  Scenario: use a table
    Given this data table:
      | first  | last     |
      | Aslak  | Hellesøy |
      | Donald | Duck     |

  Scenario: using lambdas with exceptions
    Given A lambda that declares an exception
