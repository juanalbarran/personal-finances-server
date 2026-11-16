# personal-finances-server

This is an application that will be the backend of the application that will handle my personal finances

## Input

The first input type this application will receive are the list of my monthly transactions, they are located in `./files/` the first transaction is `./files/transactions.november.2026.xlsx` so this will set the format for the next ones.
The new uploaded transactions are gonna be stored in the `./files/` directory following the pattern `transacions.<month>.<year>.xlsx`.

## Technologies

- Languages: `java`
- Framework: `spring-boot`
- Database: `postgresql`
- Migration Tool: `liquibase`

## Architecture

The application will follow the `hexagonal-architecture` the way explained by Tom Hombergs in "Get your hands dirty on clean architecture" book

## Code Style

The enforce the use of the `stream api` as much as possible, same with `lambdas`, `optionals` and `functional interfaces`.

## Development

The development will be done by phases, each phase has it's own file in `./phases/` directory
