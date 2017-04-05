# RequestSignatureApplication [work in progress]
A Spring Boot project to test interaction over REST between JS client and a Spring-boot backend by using RSA-key signed request/responses

what is done:
	- base structure

what is missing:
	- RSA-key pair creation
	- Symmetric key exchange operation
	- User registration & session handling
	- a Filter to handle requests/responses before and after accessing controller
	- calculating the hash signatures for both request and response
	- JS frontend to include signature with the requests
	- JS frontend to verify the response signature
	[possibly in future]
	- JS frontend to upload your own public key to the backend
