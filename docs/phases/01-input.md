# input

- Delete user package that you did in the `kickstart` it was for testing it is not necessary now. You can create user related classes again when it's needed.

## transactions files

The transaction file is usually an `.xlsx` file with the data of all my transactions during the moth.

- The transactions files are stored in `./../files/`
- The transactions files can be loaded directly from `./../files/` directory or can be loaded by an endpoint.
- A loaded transaction file will be stored in `./../files/` using the pattern `<year>.<month>.transactions.xlsx`, for example `2026.october.transactions.xlsx`
- Only the transactions of the month of the file are loaded, the file can contain other months
- Create the endpoint to load a transaction file
- Keep track of the loaded transactions
- When the app is started, it should check if there is a new transaction file in the `files` directory.

## Goal

Im interested to keep track of my spends during the month, i would like to know how much do i spent in groceries, in eating in restaurants, and any other spend, at the start of the month i send money to my mom's account and also i spend money to pay the cellphone, haircut and transport monthly ticket.
The idea is to store every single month this data so i can see how i'm spending the money and how i can save more in the future
