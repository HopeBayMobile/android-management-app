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

#ifndef OPENSSL_HEADER_DH_H
#define OPENSSL_HEADER_DH_H

#include <openssl/base.h>

#include <openssl/engine.h>
#include <openssl/ex_data.h>
#include <openssl/thread.h>

#if defined(__cplusplus)
extern "C" {
#endif


/* DH contains functions for performing Diffie-Hellman key agreement in
 * multiplicative groups. */


/* Allocation and destruction. */

/* DH_new returns a new, empty DH object or NULL on error. */
OPENSSL_EXPORT DH *DH_new(void);

/* DH_new_method acts the same as |DH_new| but takes an explicit |ENGINE|. */
OPENSSL_EXPORT DH *DH_new_method(const ENGINE *engine);

/* DH_free decrements the reference count of |dh| and frees it if the reference
 * count drops to zero. */
OPENSSL_EXPORT void DH_free(DH *dh);

/* DH_up_ref increments the reference count of |dh|. */
OPENSSL_EXPORT int DH_up_ref(DH *dh);


/* Standard parameters.
 *
 * These functions return new DH objects with standard parameters configured
 * that use the given ENGINE, which may be NULL. They return NULL on allocation
 * failure. */

/* These parameters are taken from RFC 5114. */

OPENSSL_EXPORT DH *DH_get_1024_160(const ENGINE *engine);
OPENSSL_EXPORT DH *DH_get_2048_224(const ENGINE *engine);
OPENSSL_EXPORT DH *DH_get_2048_256(const ENGINE *engine);


/* Parameter generation. */

#define DH_GENERATOR_2 2
#define DH_GENERATOR_5 5

/* DH_generate_parameters_ex generates a suitable Diffie-Hellman group with a
 * prime that is |prime_bits| long and stores it in |dh|. The generator of the
 * group will be |generator|, which should be |DH_GENERATOR_2| unless there's a
 * good reason to use a different value. The |cb| argument contains a callback
 * function that will be called during the generation. See the documentation in
 * |bn.h| about this. In addition to the callback invocations from |BN|, |cb|
 * will also be called with |event| equal to three when the generation is
 * complete. */
OPENSSL_EXPORT int DH_generate_parameters_ex(DH *dh, int prime_bits,
                                             int generator, BN_GENCB *cb);


/* Diffie-Hellman operations. */

/* DH_generate_key generates a new, random, private key and stores it in
 * |dh|. It returns one on success and zero on error. */
OPENSSL_EXPORT int DH_generate_key(DH *dh);

/* DH_compute_key calculates the shared key between |dh| and |peers_key| and
 * writes it as a big-endian integer into |out|, which must have |DH_size|
 * bytes of space. It returns the number of bytes written, or a negative number
 * on error. */
OPENSSL_EXPORT int DH_compute_key(uint8_t *out, const BIGNUM *peers_key,
                                  DH *dh);


/* Utility functions. */

/* DH_size returns the number of bytes in the DH group's prime. */
OPENSSL_EXPORT int DH_size(const DH *dh);

/* DH_num_bits returns the minimum number of bits needed to represent the
 * absolute value of the DH group's prime. */
OPENSSL_EXPORT unsigned DH_num_bits(const DH *dh);

#define DH_CHECK_P_NOT_PRIME 0x01
#define DH_CHECK_P_NOT_SAFE_PRIME 0x02
#define DH_CHECK_UNABLE_TO_CHECK_GENERATOR 0x04
#define DH_CHECK_NOT_SUITABLE_GENERATOR 0x08
#define DH_CHECK_Q_NOT_PRIME 0x10
#define DH_CHECK_INVALID_Q_VALUE 0x20
#define DH_CHECK_INVALID_J_VALUE 0x40

/* These are compatibility defines. */
#define DH_NOT_SUITABLE_GENERATOR DH_CHECK_NOT_SUITABLE_GENERATOR
#define DH_UNABLE_TO_CHECK_GENERATOR DH_CHECK_UNABLE_TO_CHECK_GENERATOR

/* DH_check checks the suitability of |dh| as a Diffie-Hellman group. and sets
 * |DH_CHECK_*| flags in |*out_flags| if it finds any errors. It returns one if
 * |*out_flags| was successfully set and zero on error.
 *
 * Note: these checks may be quite computationally expensive. */
OPENSSL_EXPORT int DH_check(const DH *dh, int *out_flags);

#define DH_CHECK_PUBKEY_TOO_SMALL 1
#define DH_CHECK_PUBKEY_TOO_LARGE 2

/* DH_check_pub_key checks the suitability of |pub_key| as a public key for the
 * DH group in |dh| and sets |DH_CHECK_PUBKEY_*| flags in |*out_flags| if it
 * finds any errors. It returns one if |*out_flags| was successfully set and
 * zero on error. */
OPENSSL_EXPORT int DH_check_pub_key(const DH *dh, const BIGNUM *pub_key,
                                    int *out_flags);

/* DHparams_dup allocates a fresh |DH| and copies the parameters from |dh| into
 * it. It returns the new |DH| or NULL on error. */
OPENSSL_EXPORT DH *DHparams_dup(const DH *dh);


/* ASN.1 functions. */

/* d2i_DHparams parses an ASN.1, DER encoded Diffie-Hellman parameters
 * structure from |len| bytes at |*inp|. If |ret| is not NULL then, on exit, a
 * pointer to the result is in |*ret|. If |*ret| is already non-NULL on entry
 * then the result is written directly into |*ret|, otherwise a fresh |DH| is
 * allocated. On successful exit, |*inp| is advanced past the DER structure. It
 * returns the result or NULL on error. */
OPENSSL_EXPORT DH *d2i_DHparams(DH **ret, const unsigned char **inp, long len);

/* i2d_DHparams marshals |in| to an ASN.1, DER structure. If |outp| is not NULL
 * then the result is written to |*outp| and |*outp| is advanced just past the
 * output. It returns the number of bytes in the result, whether written or
 * not, or a negative value on error. */
OPENSSL_EXPORT int i2d_DHparams(const DH *in, unsigned char **outp);


/* ex_data functions.
 *
 * See |ex_data.h| for details. */

OPENSSL_EXPORT int DH_get_ex_new_index(long argl, void *argp,
                                       CRYPTO_EX_new *new_func,
                                       CRYPTO_EX_dup *dup_func,
                                       CRYPTO_EX_free *free_func);
OPENSSL_EXPORT int DH_set_ex_data(DH *d, int idx, void *arg);
OPENSSL_EXPORT void *DH_get_ex_data(DH *d, int idx);


/* dh_method contains function pointers to override the implementation of DH.
 * See |engine.h| for details. */
struct dh_method {
  struct openssl_method_common_st common;

  /* app_data is an opaque pointer for the method to use. */
  void *app_data;

  /* init is called just before the return of |DH_new_method|. It returns one
   * on success or zero on error. */
  int (*init)(DH *dh);

  /* finish is called before |dh| is destructed. */
  void (*finish)(DH *dh);

  /* generate_parameters is called by |DH_generate_parameters_ex|. */
  int (*generate_parameters)(DH *dh, int prime_bits, int generator,
                             BN_GENCB *cb);

  /* generate_parameters is called by |DH_generate_key|. */
  int (*generate_key)(DH *dh);

  /* compute_key is called by |DH_compute_key|. */
  int (*compute_key)(DH *dh, uint8_t *out, const BIGNUM *pub_key);
};

struct dh_st {
  DH_METHOD *meth;

  BIGNUM *p;
  BIGNUM *g;
  BIGNUM *pub_key;  /* g^x */
  BIGNUM *priv_key; /* x */

  /* priv_length contains the length, in bits, of the private value. If zero,
   * the private value will be the same length as |p|. */
  unsigned priv_length;

  CRYPTO_MUTEX method_mont_p_lock;
  BN_MONT_CTX *method_mont_p;

  /* Place holders if we want to do X9.42 DH */
  BIGNUM *q;
  BIGNUM *j;
  unsigned char *seed;
  int seedlen;
  BIGNUM *counter;

  int flags;
  CRYPTO_refcount_t references;
  CRYPTO_EX_DATA ex_data;
};


#if defined(__cplusplus)
}  /* extern C */
#endif

#define DH_F_DH_new_method 100
#define DH_F_compute_key 101
#define DH_F_generate_key 102
#define DH_F_generate_parameters 103
#define DH_R_BAD_GENERATOR 100
#define DH_R_INVALID_PUBKEY 101
#define DH_R_MODULUS_TOO_LARGE 102
#define DH_R_NO_PRIVATE_VALUE 103

#endif  /* OPENSSL_HEADER_DH_H */
