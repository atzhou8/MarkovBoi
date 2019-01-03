# MarkovBot
A Discord bot that uses second-order Markov Chains to simulate sentences, created using the [Java Discord API](https://github.com/DV8FromTheWorld/JDA). Markov chains are constructed for every user in order to mimic specific people, alongside a "master" chain that contains messages sent by everybody. In addition, the bot also has access writings/speeches of several historical figures, from which it can also simulate text.

[Invite link here](https://discordapp.com/api/oauth2/authorize?client_id=420723766008610826&permissions=0&scope=bot). 

## Commands
`!help` -> Provides a list of commands. Type `!help <commandName>` to see more specific usage information for a particular command

`!read` -> Reads all of the messages in the text channel you use this command in. May take a few minutes depending on the size of your text   channel. There should be no reason to use this more than once per channel, as the bot will automatically read any messages sent in a server after it is added.

`!sim <user> <starting word  (optional)>` -> Simulates some words using the Markov chain of the specified user. Use `!sim` by itself if you just want to use the master chain with no starting word`

## Acknowledgements
Thanks to [Project Gutenberg](https://www.gutenberg.org/) and [harshilkamdar](https://github.com/harshilkamdar) for providing preloaded data that this bot uses.
