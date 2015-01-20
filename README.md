# Prints

[![Build Status](https://travis-ci.org/softprops/prints.svg)](https://travis-ci.org/softprops/prints)

a [jwt](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32) finger printer

## background

[jwt](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32) evolved out of a family of web oriented specfications targeting the second major version of oauth, [oauth2](http://oauth.net/2/) as a means of encoding structured data in a compact and tamper-proof format.

In short, jwt defines a standard means of signing arbirary data in JSON format.

## usage

A jwt token is made up of 3 parts. A (JOSE) [header](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-5) declaring information about how verify the signature of a set of claims, a set of [claims](https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4) this can actually be any arbitrary JSON data, and a signature, generated with a private key using a method specified in header information.

### headers

The only required property of a header is a signing algorithm identifier. This library current supports "none" (no signing), "HS256" (HmacSHA256), "HS384" (HmacSHA384), and "HS512" (HmacSHA512). There's room to support more. Feel free to [open a pull request](https://github.com/softprops/prints/pulls) and do so!

```scala
val header = prints.Header("HS256")
```

### claims

Claims represent some metadata meant for passing between two parties. In practice this can be any arbitrary data. Prints provides  factory methods for creating sets of claims from key value pairs of a json4s JValue.

```
val claims = prints.Claims("foo" -> "bar")
```

### making tokens

Given a `Header`, set of `Claims` and a private key you can then create a signed token

The result is an `Option` type because a header-defined signing algorithm may not be supported

```scala
val token: Option[Array[Byte]] = prints.JWT(header, claims, "secret".getBytes)
```

A valid result can then be safely used for HTTP or other transports. The result is URL safe so no additional encoding is required.

```scala
token.map(new String(_)) // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.88HN1LmGMYQTD4CYwnOoM9EqFWqSv6G1kkGI0EjNOmA=
```

### consuming (validating) tokens

Prints provides an extractor for jwt strings that unpacks the token into the 3 canonical parts. A `Header`, a `Claims` set, and a string signature

```scala
val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.88HN1LmGMYQTD4CYwnOoM9EqFWqSv6G1kkGI0EjNOmA="
val decoded = jwt match {
  case prints.JWT(header, claims, sig) =>
    // to application specific validation
  case _ => None
}
```


Doug Tangren (softprops) 2015

