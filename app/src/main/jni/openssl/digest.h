/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef OPENSSL_HEADER_DIGEST_H
#define OPENSSL_HEADER_DIGEST_H

#include <openssl/base.h>

#if defined(__cplusplus)
extern "C" {
#endif


/* Digest functions.
 *
 * An EVP_MD abstracts the details of a specific hash function allowing code to
 * deal with the concept of a "hash function" without needing to know exactly
 * which hash function it is. */


/* Hash algorithms.
 *
 * The following functions return |EVP_MD| objects that implement the named hash
 * function. */

OPENSSL_EXPORT const EVP_MD *EVP_md4(void);
OPENSSL_EXPORT const EVP_MD *EVP_md5(void);
OPENSSL_EXPORT const EVP_MD *EVP_sha1(void);
OPENSSL_EXPORT const EVP_MD *EVP_sha224(void);
OPENSSL_EXPORT const EVP_MD *EVP_sha256(void);
OPENSSL_EXPORT const EVP_MD *EVP_sha384(void);
OPENSSL_EXPORT const EVP_MD *EVP_sha512(void);

/* EVP_md5_sha1 is a TLS-specific |EVP_MD| which computes the concatenation of
 * MD5 and SHA-1, as used in TLS 1.1 and below. */
OPENSSL_EXPORT const EVP_MD *EVP_md5_sha1(void);

/* EVP_get_digestbynid returns an |EVP_MD| for the given NID, or NULL if no
 * such digest is known. */
OPENSSL_EXPORT const EVP_MD *EVP_get_digestbynid(int nid);

/* EVP_get_digestbyobj returns an |EVP_MD| for the given |ASN1_OBJECT|, or NULL
 * if no such digest is known. */
OPENSSL_EXPORT const EVP_MD *EVP_get_digestbyobj(const ASN1_OBJECT *obj);


/* Digest contexts.
 *
 * An EVP_MD_CTX represents the state of a specific digest operation in
 * progress. */

/* EVP_MD_CTX_init initialises an, already allocated, |EVP_MD_CTX|. */
OPENSSL_EXPORT void EVP_MD_CTX_init(EVP_MD_CTX *ctx);

/* EVP_MD_CTX_create allocates and initialises a fresh |EVP_MD_CTX| and returns
 * it, or NULL on allocation failure. */
OPENSSL_EXPORT EVP_MD_CTX *EVP_MD_CTX_create(void);

/* EVP_MD_CTX_cleanup frees any resources owned by |ctx| and resets it to a
 * freshly initialised state. It does not free |ctx| itself. It returns one. */
OPENSSL_EXPORT int EVP_MD_CTX_cleanup(EVP_MD_CTX *ctx);

/* EVP_MD_CTX_destroy calls |EVP_MD_CTX_cleanup| and then frees |ctx| itself. */
OPENSSL_EXPORT void EVP_MD_CTX_destroy(EVP_MD_CTX *ctx);

/* EVP_MD_CTX_copy_ex sets |out|, which must already be initialised, to be a
 * copy of |in|. It returns one on success and zero on error. */
OPENSSL_EXPORT int EVP_MD_CTX_copy_ex(EVP_MD_CTX *out, const EVP_MD_CTX *in);


/* Digest operations. */

/* EVP_DigestInit_ex configures |ctx|, which must already have been
 * initialised, for a fresh hashing operation using |type|. It returns one on
 * success and zero otherwise. */
OPENSSL_EXPORT int EVP_DigestInit_ex(EVP_MD_CTX *ctx, const EVP_MD *type,
                                     ENGINE *engine);

/* EVP_DigestInit acts like |EVP_DigestInit_ex| except that |ctx| is
 * initialised before use. */
OPENSSL_EXPORT int EVP_DigestInit(EVP_MD_CTX *ctx, const EVP_MD *type);

/* EVP_DigestUpdate hashes |len| bytes from |data| into the hashing operation
 * in |ctx|. It returns one. */
OPENSSL_EXPORT int EVP_DigestUpdate(EVP_MD_CTX *ctx, const void *data,
                                    size_t len);

/* EVP_MAX_MD_SIZE is the largest digest size supported. Functions that output
 * a digest generally require the buffer have at least this much space. */
#define EVP_MAX_MD_SIZE 64 /* SHA-512 is the longest so far. */

/* EVP_DigestFinal_ex finishes the digest in |ctx| and writes the output to
 * |md_out|. At most |EVP_MAX_MD_SIZE| bytes are written. If |out_size| is not
 * NULL then |*out_size| is set to the number of bytes written. It returns one.
 * After this call, the hash cannot be updated or finished again until
 * |EVP_DigestInit_ex| is called to start another hashing operation. */
OPENSSL_EXPORT int EVP_DigestFinal_ex(EVP_MD_CTX *ctx, uint8_t *md_out,
                                      unsigned int *out_size);

/* EVP_DigestFinal acts like |EVP_DigestFinal_ex| except that
 * |EVP_MD_CTX_cleanup| is called on |ctx| before returning. */
OPENSSL_EXPORT int EVP_DigestFinal(EVP_MD_CTX *ctx, uint8_t *md_out,
                                   unsigned int *out_size);

/* EVP_Digest performs a complete hashing operation in one call. It hashes
 * |len| bytes from |data| and writes the digest to |md_out|. At most
 * |EVP_MAX_MD_SIZE| bytes are written. If |out_size| is not NULL then
 * |*out_size| is set to the number of bytes written. It returns one on success
 * and zero otherwise. */
OPENSSL_EXPORT int EVP_Digest(const void *data, size_t len, uint8_t *md_out,
                              unsigned int *md_out_size, const EVP_MD *type,
                              ENGINE *impl);


/* Digest function accessors.
 *
 * These functions allow code to learn details about an abstract hash
 * function. */

/* EVP_MD_type returns a NID identifing |md|. (For example, |NID_sha256|.) */
OPENSSL_EXPORT int EVP_MD_type(const EVP_MD *md);

/* EVP_MD_flags returns the flags for |md|, which is a set of |EVP_MD_FLAG_*|
 * values, ORed together. */
OPENSSL_EXPORT uint32_t EVP_MD_flags(const EVP_MD *md);

/* EVP_MD_size returns the digest size of |md|, in bytes. */
OPENSSL_EXPORT size_t EVP_MD_size(const EVP_MD *md);

/* EVP_MD_block_size returns the native block-size of |md|. */
OPENSSL_EXPORT size_t EVP_MD_block_size(const EVP_MD *md);

/* EVP_MD_FLAG_PKEY_DIGEST indicates the the digest function is used with a
 * specific public key in order to verify signatures. (For example,
 * EVP_dss1.) */
#define EVP_MD_FLAG_PKEY_DIGEST 1

/* EVP_MD_FLAG_DIGALGID_ABSENT indicates that the parameter type in an X.509
 * DigestAlgorithmIdentifier representing this digest function should be
 * undefined rather than NULL. */
#define EVP_MD_FLAG_DIGALGID_ABSENT 2


/* Deprecated functions. */

/* EVP_MD_CTX_copy sets |out|, which must /not/ be initialised, to be a copy of
 * |in|. It returns one on success and zero on error. */
OPENSSL_EXPORT int EVP_MD_CTX_copy(EVP_MD_CTX *out, const EVP_MD_CTX *in);

/* EVP_add_digest does nothing and returns one. It exists only for
 * compatibility with OpenSSL. */
OPENSSL_EXPORT int EVP_add_digest(const EVP_MD *digest);

/* EVP_get_cipherbyname returns an |EVP_MD| given a human readable name in
 * |name|, or NULL if the name is unknown. */
OPENSSL_EXPORT const EVP_MD *EVP_get_digestbyname(const char *);


/* Digest operation accessors. */

/* EVP_MD_CTX_md returns the underlying digest function, or NULL if one has not
 * been set. */
OPENSSL_EXPORT const EVP_MD *EVP_MD_CTX_md(const EVP_MD_CTX *ctx);

/* EVP_MD_CTX_size returns the digest size of |ctx|. It will crash if a digest
 * hasn't been set on |ctx|. */
OPENSSL_EXPORT unsigned EVP_MD_CTX_size(const EVP_MD_CTX *ctx);

/* EVP_MD_CTX_block_size returns the block size of the digest function used by
 * |ctx|. It will crash if a digest hasn't been set on |ctx|. */
OPENSSL_EXPORT unsigned EVP_MD_CTX_block_size(const EVP_MD_CTX *ctx);

/* EVP_MD_CTX_type returns a NID describing the digest function used by |ctx|.
 * (For example, |NID_sha256|.) It will crash if a digest hasn't been set on
 * |ctx|. */
OPENSSL_EXPORT int EVP_MD_CTX_type(const EVP_MD_CTX *ctx);


struct evp_md_pctx_ops;

struct env_md_ctx_st {
  /* digest is the underlying digest function, or NULL if not set. */
  const EVP_MD *digest;
  /* flags is the OR of a number of |EVP_MD_CTX_FLAG_*| values. */
  uint32_t flags;
  /* md_data points to a block of memory that contains the hash-specific
   * context. */
  void *md_data;
  /* update is usually copied from |digest->update| but can differ in some
   * cases, i.e. HMAC.
   * TODO(davidben): Remove this hook once |EVP_PKEY_HMAC| is gone. */
  void (*update)(EVP_MD_CTX *ctx, const void *data, size_t count);

  /* pctx is an opaque (at this layer) pointer to additional context that
   * EVP_PKEY functions may store in this object. */
  EVP_PKEY_CTX *pctx;

  /* pctx_ops, if not NULL, points to a vtable that contains functions to
   * manipulate |pctx|. */
  const struct evp_md_pctx_ops *pctx_ops;
} /* EVP_MD_CTX */;


#if defined(__cplusplus)
}  /* extern C */
#endif

#define DIGEST_F_EVP_DigestInit_ex 100
#define DIGEST_F_EVP_MD_CTX_copy_ex 101
#define DIGEST_R_INPUT_NOT_INITIALIZED 100

#endif  /* OPENSSL_HEADER_DIGEST_H */
